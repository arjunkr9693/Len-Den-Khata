package com.arjun.len_denkhata.data.database.transactions.monthbook

import androidx.room.*

@Dao
interface MonthBookTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: MonthBookTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: MonthBookTransaction)

    @Query("SELECT * FROM month_book_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): kotlinx.coroutines.flow.Flow<List<MonthBookTransaction>>

    @Query("SELECT * FROM month_book_transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: String): kotlinx.coroutines.flow.Flow<List<MonthBookTransaction>>

    @Query("SELECT SUM(amount) FROM month_book_transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): kotlinx.coroutines.flow.Flow<Double?>

    @Query("SELECT SUM(amount) FROM month_book_transactions WHERE type = 'EXPENSE'")
    fun getTotalExpense(): kotlinx.coroutines.flow.Flow<Double?>

    @Query("SELECT SUM(amount) FROM month_book_transactions WHERE type = 'EXPENSE' AND expenseCategory = :category")
    fun getTotalExpenseByCategory(category: String): kotlinx.coroutines.flow.Flow<Double?>
}