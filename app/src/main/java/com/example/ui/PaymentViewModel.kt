package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PaymentViewModel(
    application: Application,
    private val repository: PaymentRepository
) : AndroidViewModel(application) {

    private val _allTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val allTransactions: StateFlow<List<TransactionEntity>> = _allTransactions.asStateFlow()

    private val _allBankAccounts = MutableStateFlow<List<BankAccountEntity>>(emptyList())
    val allBankAccounts: StateFlow<List<BankAccountEntity>> = _allBankAccounts.asStateFlow()

    private val _allBills = MutableStateFlow<List<SavedBillEntity>>(emptyList())
    val allBills: StateFlow<List<SavedBillEntity>> = _allBills.asStateFlow()

    // Active transaction flow states
    var activeRecipientName = MutableStateFlow("")
    var activeRecipientUpi = MutableStateFlow("")
    var activeAmount = MutableStateFlow("")
    var activeCategory = MutableStateFlow("Transfer")
    var selectedBankId = MutableStateFlow(-1)

    // UPI PIN screen states
    var pinScreenVisible = MutableStateFlow(false)
    var paymentStatus = MutableStateFlow("IDLE") // IDLE, AUTHENTICATING, PROCESSING, SUCCESS, FAILED
    var paymentErrorMessage = MutableStateFlow("")
    var activeTransactionId = MutableStateFlow("")

    // Active bill details
    var selectedBill = MutableStateFlow<SavedBillEntity?>(null)

    // AI Spending Coach states
    var aiConversation = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("Smart Spend Coach", "Hello! I am your PhonePe AI Advisor. Ask me anything about your spending, budget, or secure UPI practices! e.g., 'Analyze my spending' or 'Give me safety tips'", false)
    ))
    var isAiGenerating = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            try {
                // Initialize database with default mock data on first launch
                repository.prepopulateIfEmpty()
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "Prepopulation failed: ${e.message}", e)
            }

            // Sync database flows
            repository.allTransactions.collectLatest { _allTransactions.value = it }
        }
        viewModelScope.launch {
            repository.allBankAccounts.collectLatest { accounts ->
                _allBankAccounts.value = accounts
                if (selectedBankId.value == -1 && accounts.isNotEmpty()) {
                    selectedBankId.value = accounts.firstOrNull { it.isPrimary }?.id ?: accounts.first().id
                }
            }
        }
        viewModelScope.launch {
            repository.allBills.collectLatest { _allBills.value = it }
        }
    }

    // Process scanned QR code
    fun processScannedQR(qrText: String): Boolean {
        if (qrText.isBlank()) return false
        
        try {
            // standard upi schema: upi://pay?pa=recipient@upi&pn=Recipient%20Name&am=500
            if (qrText.startsWith("upi://pay")) {
                val params = qrText.substringAfter("?").split("&").associate {
                    val pair = it.split("=")
                    if (pair.size == 2) pair[0] to java.net.URLDecoder.decode(pair[1], "UTF-8") else pair[0] to ""
                }
                activeRecipientUpi.value = params["pa"] ?: "scanned@upi"
                activeRecipientName.value = params["pn"] ?: "Scanned Merchant"
                activeAmount.value = params["am"] ?: ""
                activeCategory.value = "Merchant Pay"
                return true
            } else if (qrText.contains("@")) {
                // Simple UPI ID
                activeRecipientUpi.value = qrText.trim()
                activeRecipientName.value = qrText.substringBefore("@").replaceFirstChar { it.uppercase() }
                activeAmount.value = ""
                activeCategory.value = "Transfer"
                return true
            } else {
                // Check if it's bank format: bank://acc=1234&ifsc=HDFC&name=John
                if (qrText.startsWith("bank://")) {
                    val query = qrText.substringAfter("bank://")
                    val params = query.split("&").associate {
                        val pair = it.split("=")
                        if (pair.size == 2) pair[0] to java.net.URLDecoder.decode(pair[1], "UTF-8") else pair[0] to ""
                    }
                    val name = params["name"] ?: "Bank Account Transfer"
                    val acc = params["acc"] ?: "XXXX 0000"
                    val ifsc = params["ifsc"] ?: "IFSCCODE"
                    
                    activeRecipientName.value = name
                    activeRecipientUpi.value = "$acc (IFSC: $ifsc)"
                    activeAmount.value = ""
                    activeCategory.value = "Transfer"
                    return true
                } else {
                    // Fallback to simple scan string
                    activeRecipientUpi.value = "qr.merchant@phonepe"
                    activeRecipientName.value = qrText
                    activeAmount.value = ""
                    activeCategory.value = "Merchant Pay"
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e("PaymentViewModel", "QR parsing failed: ${e.message}")
            return false
        }
    }

    // Initiate general payment
    fun initiatePayment(name: String, upi: String, amount: String, category: String = "Transfer") {
        activeRecipientName.value = name
        activeRecipientUpi.value = upi
        activeAmount.value = amount
        activeCategory.value = category
        pinScreenVisible.value = true
        paymentStatus.value = "INPUT"
    }

    // Execute payment with entered PIN
    fun executePayment(pin: String) {
        viewModelScope.launch {
            paymentStatus.value = "AUTHENTICATING"
            delay(1200) // Simulated secure authentication delay

            val fundingAccount = _allBankAccounts.value.find { it.id == selectedBankId.value }
            if (fundingAccount == null) {
                paymentStatus.value = "FAILED"
                paymentErrorMessage.value = "Funding source bank not found."
                return@launch
            }

            if (fundingAccount.upiPin != pin) {
                paymentStatus.value = "FAILED"
                paymentErrorMessage.value = "Incorrect UPI PIN. Please try again."
                // Record failed transaction
                val tx = TransactionEntity(
                    title = activeRecipientName.value,
                    subtitle = "Failed Transfer (Incorrect PIN)",
                    amount = -(activeAmount.value.toDoubleOrNull() ?: 0.0),
                    status = "FAILED",
                    type = "SEND",
                    upiId = activeRecipientUpi.value,
                    category = activeCategory.value
                )
                repository.insertTransaction(tx)
                return@launch
            }

            val payAmount = activeAmount.value.toDoubleOrNull() ?: 0.0
            if (payAmount <= 0) {
                paymentStatus.value = "FAILED"
                paymentErrorMessage.value = "Invalid transfer amount."
                return@launch
            }

            if (fundingAccount.balance < payAmount) {
                paymentStatus.value = "FAILED"
                paymentErrorMessage.value = "Insufficient balance in ${fundingAccount.bankName}."
                // Record failed transaction
                val tx = TransactionEntity(
                    title = activeRecipientName.value,
                    subtitle = "Failed (Insufficient Balance)",
                    amount = -payAmount,
                    status = "FAILED",
                    type = "SEND",
                    upiId = activeRecipientUpi.value,
                    category = activeCategory.value
                )
                repository.insertTransaction(tx)
                return@launch
            }

            paymentStatus.value = "PROCESSING"
            delay(1500) // Simulated processing through NPCI servers

            try {
                // Deduct balance
                val newBalance = fundingAccount.balance - payAmount
                repository.updateBalance(fundingAccount.id, newBalance)

                // Record success transaction
                val txnIdStr = "TXN" + System.currentTimeMillis().toString().takeLast(9)
                activeTransactionId.value = txnIdStr

                val tx = TransactionEntity(
                    title = activeRecipientName.value,
                    subtitle = "Paid successfully from ${fundingAccount.bankName}",
                    amount = -payAmount,
                    status = "SUCCESS",
                    type = if (activeCategory.value == "RECHARGE") "RECHARGE" else if (activeCategory.value == "Transfer") "SEND" else "BILL_PAY",
                    upiId = activeRecipientUpi.value,
                    category = activeCategory.value
                )
                repository.insertTransaction(tx)

                // If this is a bill payment, mark the bill as paid
                selectedBill.value?.let { bill ->
                    val updatedBill = bill.copy(isPaid = true)
                    repository.updateBill(updatedBill)
                    selectedBill.value = null
                }

                paymentStatus.value = "SUCCESS"
            } catch (e: Exception) {
                paymentStatus.value = "FAILED"
                paymentErrorMessage.value = "Transaction failed: ${e.localizedMessage}"
            }
        }
    }

    // Pay bill
    fun payBill(bill: SavedBillEntity) {
        selectedBill.value = bill
        initiatePayment(
            name = bill.billerName,
            upi = "${bill.consumerNumber}@${bill.category.lowercase()}",
            amount = bill.amount.toString(),
            category = bill.category
        )
    }

    // Self Transfer from one account to another
    fun selfTransfer(fromAccountId: Int, toAccountId: Int, amount: Double, pin: String) {
        viewModelScope.launch {
            if (fromAccountId == toAccountId) {
                paymentStatus.value = "FAILED"
                paymentErrorMessage.value = "Source and destination accounts must be different."
                return@launch
            }

            paymentStatus.value = "AUTHENTICATING"
            delay(1000)

            val fromAccount = _allBankAccounts.value.find { it.id == fromAccountId }
            val toAccount = _allBankAccounts.value.find { it.id == toAccountId }

            if (fromAccount == null || toAccount == null) {
                paymentStatus.value = "FAILED"
                paymentErrorMessage.value = "Invalid bank accounts selected."
                return@launch
            }

            if (fromAccount.upiPin != pin) {
                paymentStatus.value = "FAILED"
                paymentErrorMessage.value = "Incorrect UPI PIN."
                return@launch
            }

            if (fromAccount.balance < amount) {
                paymentStatus.value = "FAILED"
                paymentErrorMessage.value = "Insufficient balance in ${fromAccount.bankName}."
                return@launch
            }

            paymentStatus.value = "PROCESSING"
            delay(1000)

            // Deduct & Add
            repository.updateBalance(fromAccount.id, fromAccount.balance - amount)
            repository.updateBalance(toAccount.id, toAccount.balance + amount)

            val tx = TransactionEntity(
                title = "Self Transfer",
                subtitle = "Transferred from ${fromAccount.bankName} to ${toAccount.bankName}",
                amount = -amount,
                status = "SUCCESS",
                type = "SELF",
                upiId = toAccount.upiId,
                category = "Transfer"
            )
            repository.insertTransaction(tx)

            paymentStatus.value = "SUCCESS"
            activeRecipientName.value = "Self Transfer"
            activeRecipientUpi.value = "To ${toAccount.bankName}"
            activeAmount.value = amount.toString()
            activeCategory.value = "Transfer"
            activeTransactionId.value = "ST" + System.currentTimeMillis().toString().takeLast(9)
        }
    }

    // Add bank account
    fun addBankAccount(bankName: String, balance: Double = 5000.00) {
        viewModelScope.launch {
            val randomNum = (1000..9999).random()
            val upiSuffix = when (bankName) {
                "HDFC Bank" -> "hdfc"
                "ICICI Bank" -> "icici"
                "SBI" -> "sbi"
                else -> "axl"
            }
            val account = BankAccountEntity(
                bankName = bankName,
                accountNumber = "XXXX $randomNum",
                ifsc = "IFSC${(10000..99999).random()}",
                balance = balance,
                isPrimary = _allBankAccounts.value.isEmpty(),
                upiId = "user$randomNum@$upiSuffix"
            )
            repository.insertBankAccount(account)
        }
    }

    // Ask AI Coach for advice
    fun askAiCoach(userMessage: String) {
        if (userMessage.isBlank()) return
        
        val chatList = _allConversation.value.toMutableList()
        chatList.add(ChatMessage("You", userMessage, true))
        _allConversation.value = chatList

        viewModelScope.launch {
            isAiGenerating.value = true
            try {
                val advice = GeminiService.getFinancialAdvice(userMessage, _allTransactions.value)
                val responseList = _allConversation.value.toMutableList()
                responseList.add(ChatMessage("Smart Spend Coach", advice, false))
                _allConversation.value = responseList
            } catch (e: Exception) {
                val responseList = _allConversation.value.toMutableList()
                responseList.add(ChatMessage("Smart Spend Coach", "Sorry, I had trouble parsing that. Please try again! Error: ${e.message}", false))
                _allConversation.value = responseList
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun clearActivePaymentState() {
        paymentStatus.value = "IDLE"
        paymentErrorMessage.value = ""
        activeTransactionId.value = ""
        pinScreenVisible.value = false
    }

    private val _allConversation = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("Smart Spend Coach", "Hello! I am your PhonePe AI Advisor. Ask me anything about your spending, budget, or secure UPI practices! e.g., 'Analyze my spending' or 'Give me safety tips'", false)
    ))

    init {
        viewModelScope.launch {
            _allConversation.collectLatest {
                aiConversation.value = it
            }
        }
    }
}

data class ChatMessage(
    val sender: String,
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class PaymentViewModelFactory(
    private val application: Application,
    private val repository: PaymentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
