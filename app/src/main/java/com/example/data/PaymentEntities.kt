package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // SUCCESS, PENDING, FAILED
    val type: String, // SEND, RECEIVE, BILL_PAY, RECHARGE, SELF
    val upiId: String,
    val category: String // Transfer, Mobile, Electricity, DTH, Water, Gas, etc.
)

@Entity(tableName = "bank_accounts")
data class BankAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bankName: String,
    val accountNumber: String, // Masked or partial e.g. "XXXX 1234"
    val ifsc: String,
    val balance: Double,
    val isPrimary: Boolean,
    val upiId: String,
    val upiPin: String = "1234"
)

@Entity(tableName = "saved_bills")
data class SavedBillEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val billerName: String,
    val category: String, // Mobile, Electricity, DTH, Water, Gas
    val consumerNumber: String,
    val amount: Double,
    val dueDate: String,
    val isPaid: Boolean = false
)
