package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentRepository(private val paymentDao: PaymentDao) {
    val allTransactions: Flow<List<TransactionEntity>> = paymentDao.getAllTransactions()
    val allBankAccounts: Flow<List<BankAccountEntity>> = paymentDao.getAllBankAccounts()
    val allBills: Flow<List<SavedBillEntity>> = paymentDao.getAllBills()

    suspend fun insertTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        paymentDao.insertTransaction(transaction)
    }

    suspend fun insertBankAccount(account: BankAccountEntity): Long = withContext(Dispatchers.IO) {
        paymentDao.insertBankAccount(account)
    }

    suspend fun updateBankAccount(account: BankAccountEntity) = withContext(Dispatchers.IO) {
        paymentDao.updateBankAccount(account)
    }

    suspend fun deleteBankAccount(account: BankAccountEntity) = withContext(Dispatchers.IO) {
        paymentDao.deleteBankAccount(account)
    }

    suspend fun getBankAccountById(id: Int): BankAccountEntity? = withContext(Dispatchers.IO) {
        paymentDao.getBankAccountById(id)
    }

    suspend fun updateBalance(id: Int, newBalance: Double) = withContext(Dispatchers.IO) {
        paymentDao.updateBalance(id, newBalance)
    }

    suspend fun insertBill(bill: SavedBillEntity): Long = withContext(Dispatchers.IO) {
        paymentDao.insertBill(bill)
    }

    suspend fun updateBill(bill: SavedBillEntity) = withContext(Dispatchers.IO) {
        paymentDao.updateBill(bill)
    }

    suspend fun deleteBill(bill: SavedBillEntity) = withContext(Dispatchers.IO) {
        paymentDao.deleteBill(bill)
    }

    suspend fun prepopulateIfEmpty() = withContext(Dispatchers.IO) {
        val accounts = paymentDao.getAllBankAccounts().first()
        if (accounts.isEmpty()) {
            // Add default bank accounts
            paymentDao.insertBankAccount(
                BankAccountEntity(
                    bankName = "HDFC Bank",
                    accountNumber = "XXXX 4921",
                    ifsc = "HDFC0000104",
                    balance = 24500.00,
                    isPrimary = true,
                    upiId = "user@hdfc"
                )
            )
            paymentDao.insertBankAccount(
                BankAccountEntity(
                    bankName = "ICICI Bank",
                    accountNumber = "XXXX 8392",
                    ifsc = "ICIC0000020",
                    balance = 8350.50,
                    isPrimary = false,
                    upiId = "user@icici"
                )
            )
            paymentDao.insertBankAccount(
                BankAccountEntity(
                    bankName = "State Bank of India",
                    accountNumber = "XXXX 1024",
                    ifsc = "SBIN0001234",
                    balance = 53200.75,
                    isPrimary = false,
                    upiId = "user@sbi"
                )
            )

            // Add default bills
            paymentDao.insertBill(
                SavedBillEntity(
                    billerName = "BESCOM Electricity",
                    category = "Electricity",
                    consumerNumber = "129483019",
                    amount = 940.00,
                    dueDate = "June 30, 2026",
                    isPaid = false
                )
            )
            paymentDao.insertBill(
                SavedBillEntity(
                    billerName = "Airtel Postpaid Mobile",
                    category = "Mobile",
                    consumerNumber = "9876543210",
                    amount = 499.00,
                    dueDate = "June 28, 2026",
                    isPaid = false
                )
            )
            paymentDao.insertBill(
                SavedBillEntity(
                    billerName = "Tata Play DTH",
                    category = "DTH",
                    consumerNumber = "DTH103948",
                    amount = 350.00,
                    dueDate = "June 25, 2026",
                    isPaid = true
                )
            )
            paymentDao.insertBill(
                SavedBillEntity(
                    billerName = "Indane Gas",
                    category = "Gas",
                    consumerNumber = "GAS88231",
                    amount = 1050.00,
                    dueDate = "July 05, 2026",
                    isPaid = false
                )
            )

            // Add default transactions
            paymentDao.insertTransaction(
                TransactionEntity(
                    title = "Aman Verma",
                    subtitle = "Paid via UPI",
                    amount = -1500.00,
                    status = "SUCCESS",
                    type = "SEND",
                    upiId = "aman@okhdfcbank",
                    category = "Transfer"
                )
            )
            paymentDao.insertTransaction(
                TransactionEntity(
                    title = "Cashback Received",
                    subtitle = "PhonePe Rewards",
                    amount = 50.00,
                    status = "SUCCESS",
                    type = "RECEIVE",
                    upiId = "phonepe@reward",
                    category = "Reward"
                )
            )
            paymentDao.insertTransaction(
                TransactionEntity(
                    title = "BESCOM Electricity",
                    subtitle = "Utility Payment",
                    amount = -850.00,
                    status = "SUCCESS",
                    type = "BILL_PAY",
                    upiId = "bescom@billdesk",
                    category = "Electricity"
                )
            )
            paymentDao.insertTransaction(
                TransactionEntity(
                    title = "Jio Mobile Prepaid",
                    subtitle = "Recharge SUCCESS",
                    amount = -299.00,
                    status = "SUCCESS",
                    type = "RECHARGE",
                    upiId = "jio@recharge",
                    category = "Mobile"
                )
            )
        }
    }
}
