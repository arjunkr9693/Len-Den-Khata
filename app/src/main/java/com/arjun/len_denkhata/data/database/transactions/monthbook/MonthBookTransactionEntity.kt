package com.arjun.len_denkhata.data.database.transactions.monthbook

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arjun.len_denkhata.data.utils.UserSession
import java.util.Date

@Entity(tableName = "month_book_transactions")
data class MonthBookTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // For Room auto-generation
    val ownerId: String = UserSession.phoneNumber!!,
    val amount: Double,
    val description: String,
    val type: MonthBookTransactionType,
    val expenseCategory: MonthBookExpenseCategory? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val firebaseId: String? = null,
    val edited: Boolean = false,
    val editedOn: Long? = null,
    val deleted: Boolean = false
)

enum class MonthBookTransactionType {
    INCOME,
    EXPENSE
}

enum class MonthBookExpenseCategory {
    GENERAL,
    WORK_RELATED
}