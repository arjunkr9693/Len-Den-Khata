package com.arjun.len_denkhata.data.repository

import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionDao
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionEntity
import com.arjun.len_denkhata.data.utils.MonthBookSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MonthBookRepository @Inject constructor(
    private val monthBookTransactionDao: MonthBookTransactionDao,
    private val syncManager: MonthBookSyncManager // Inject the SyncManager
) {
    val allTransactions: Flow<List<MonthBookTransactionEntity>> = monthBookTransactionDao.getAllTransactions().conflate()

    fun getTransactionsByType(type: String): Flow<List<MonthBookTransactionEntity>> =
        monthBookTransactionDao.getTransactionsByType(type).conflate()

    val totalIncome: Flow<Double?> = monthBookTransactionDao.getTotalIncome().conflate()
    val totalExpense: Flow<Double?> = monthBookTransactionDao.getTotalExpense().conflate()

    suspend fun insertTransaction(transaction: MonthBookTransactionEntity, initialStoringWhenLogin: Boolean = false) = withContext(Dispatchers.IO) {
        val id = monthBookTransactionDao.insertTransaction(transaction)
            // Enqueue for upload after successful local insertion
        if (!initialStoringWhenLogin) {
            val newTransaction = transaction.copy(id = id) // Ensure ID is set
            syncManager.enqueueTransactionForUpload(newTransaction)
        }
    }

    suspend fun updateTransaction(transaction: MonthBookTransactionEntity) = withContext(Dispatchers.IO) {
        monthBookTransactionDao.updateTransaction(transaction)
        // Enqueue for update
        syncManager.enqueueTransactionForUpdate(transaction)
    }

    suspend fun deleteTransaction(transaction: MonthBookTransactionEntity) = withContext(Dispatchers.IO) {
        monthBookTransactionDao.updateTransaction(transaction.copy(deleted = true))
        // Enqueue for delete
        syncManager.enqueueTransactionForDelete(transaction.id)
    }

    fun getTotalExpenseByCategory(category: String): Flow<Double?> =
        monthBookTransactionDao.getTotalExpenseByCategory(category).conflate()

    fun getTransactionById(transactionId: Long): Flow<MonthBookTransactionEntity?> {
        return monthBookTransactionDao.getTransactionById(transactionId).conflate()
    }

//    suspend fun getTransactionByIdOnce(transactionId: Long): MonthBookTransactionEntity? = withContext(Dispatchers.IO) {
//        monthBookTransactionDao.getTransactionByIdOnce(transactionId)
//    }
}