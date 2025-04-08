package com.arjun.len_denkhata.data.database.transactions.monthbook

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "month_book_transactions")
data class MonthBookTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // For Room auto-generation
    val amount: Double,
    val description: String,
    val type: TransactionType,
    val expenseCategory: ExpenseCategory? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class ExpenseCategory {
    GENERAL,
    WORK_RELATED
}