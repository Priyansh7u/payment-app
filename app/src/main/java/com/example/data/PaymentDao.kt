package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    // Bank Accounts
    @Query("SELECT * FROM bank_accounts ORDER BY isPrimary DESC, bankName ASC")
    fun getAllBankAccounts(): Flow<List<BankAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankAccount(account: BankAccountEntity): Long

    @Update
    suspend fun updateBankAccount(account: BankAccountEntity)

    @Delete
    suspend fun deleteBankAccount(account: BankAccountEntity)

    @Query("SELECT * FROM bank_accounts WHERE id = :id")
    suspend fun getBankAccountById(id: Int): BankAccountEntity?

    @Query("UPDATE bank_accounts SET balance = :newBalance WHERE id = :id")
    suspend fun updateBalance(id: Int, newBalance: Double)

    // Saved Bills
    @Query("SELECT * FROM saved_bills ORDER BY isPaid ASC, dueDate ASC")
    fun getAllBills(): Flow<List<SavedBillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: SavedBillEntity): Long

    @Update
    suspend fun updateBill(bill: SavedBillEntity)

    @Delete
    suspend fun deleteBill(bill: SavedBillEntity)
}
