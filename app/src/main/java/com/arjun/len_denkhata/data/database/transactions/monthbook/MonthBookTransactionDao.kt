package com.arjun.len_denkhata.data.database.transactions.monthbook

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthBookTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: MonthBookTransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: MonthBookTransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: MonthBookTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMultipleTransactions(transactions: List<MonthBookTransactionEntity>)

    @Query("SELECT * FROM month_book_transactions WHERE deleted = 0 ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<MonthBookTransactionEntity>>

    @Query("SELECT * FROM month_book_transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: String): Flow<List<MonthBookTransactionEntity>>

    @Query("SELECT SUM(amount) FROM month_book_transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM month_book_transactions WHERE type = 'EXPENSE'")
    fun getTotalExpense(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM month_book_transactions WHERE type = 'EXPENSE' AND expenseCategory = :category")
    fun getTotalExpenseByCategory(category: String): Flow<Double?>

    @Query("SELECT * FROM month_book_transactions WHERE id = :transactionId")
    fun getTransactionById(transactionId: Long): Flow<MonthBookTransactionEntity?>

    @Query("SELECT * FROM month_book_transactions WHERE id = :transactionId")
    fun getTransactionByIdOnce(transactionId: Long): MonthBookTransactionEntity?

    @Query("DELETE FROM month_book_transactions WHERE id = :transactionId")
    fun deleteTransactionById(transactionId: Long)
}