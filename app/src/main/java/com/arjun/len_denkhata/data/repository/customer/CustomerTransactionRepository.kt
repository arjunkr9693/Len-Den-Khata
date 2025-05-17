package com.arjun.len_denkhata.data.repository.customer

import android.util.Log
import com.arjun.len_denkhata.data.database.SyncStatus
import com.arjun.len_denkhata.data.database.CustomerSyncStatusDao
import com.arjun.len_denkhata.data.database.SyncStatusEntity
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionDao
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.arjun.len_denkhata.data.utils.CustomerSyncManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

class CustomerTransactionRepository @Inject constructor(
    private val customerTransactionDao: CustomerTransactionDao,
    private val firestore: FirebaseFirestore,
    private val customerSyncManager: CustomerSyncManager,
    private val syncStatusDao: CustomerSyncStatusDao,
    private val customerRepository: CustomerRepository
) {

    init {
        Log.d("CustomerTransactionRepo", "Repository initialized")
    }

    private val _todayDue = MutableStateFlow(0.0)
    val todayDue: StateFlow<Double> = _todayDue

    suspend fun insertCustomerTransaction(
        transaction: CustomerTransactionEntity,
        initialStoringWhenLoggin: Boolean = false
    ) {
        try {
            val transactionId = customerTransactionDao.insert(transaction)
            val insertedTransaction = transaction.copy(id = transactionId)
            customerRepository.updateCustomerBalance(insertedTransaction.customerId, insertedTransaction.amount, insertedTransaction.isCredit)
            if (insertedTransaction.isMadeByOwner && !initialStoringWhenLoggin) {
                customerSyncManager.enqueueTransactionForUpload(insertedTransaction)
            }
        } catch (e: Exception) {
            Log.e("CustomerTransactionRepo", "Error inserting transaction: ${e.message}")
        }
    }

    suspend fun calculateTodayDue() = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()

        val todayStart = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayEnd = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        customerTransactionDao.getTodayDebitTransactions(todayStart, todayEnd)
            .collectLatest { debitTransactions ->

                Log.d("TotalDue", debitTransactions.toString())
                val dueAmount = debitTransactions.sumOf { it.amount }
                Log.d("TotalDue", dueAmount.toString())
                _todayDue.value = dueAmount
            }
    }

    fun getCustomerTransactionsByCustomerId(customerId: String): Flow<List<CustomerTransactionEntity>> {
        return customerTransactionDao.getTransactionsByCustomerId(customerId)
    }

    suspend fun deleteTransaction(transactionId: Long) {
        try {
            val transaction = customerTransactionDao.getTransactionByTransactionId(transactionId)
            if (transaction != null) {
                customerTransactionDao.update(transaction.copy(isDeleted = true))
            }
            transaction?.let { customerRepository.updateCustomerBalance(it.customerId, transaction.amount, isCredit = !transaction.isCredit) }
            if (transaction != null) {
                if (transaction.isMadeByOwner) {
                    customerSyncManager.enqueueTransactionForDelete(transaction.id)
                }
            }
        } catch (e: Exception) {
            Log.e("CustomerTransactionRepo", "Error deleting transaction: ${e.message}")
        }
    }

    suspend fun updateTransaction(transaction: CustomerTransactionEntity, originAmount: Double) {
        try {
            val amountDifference = transaction.amount - originAmount
            customerTransactionDao.update(transaction)
            customerRepository.updateCustomerBalance(
                transaction.customerId,
                amountDifference,
                transaction.isCredit,
                isEditing = true
            )
            if (transaction.isMadeByOwner) {
                customerSyncManager.enqueueTransactionForUpdate(transaction)
            }
        } catch (e: Exception) {
            Log.e("CustomerTransactionRepo", "Error updating transaction: ${e.message}")
        }
    }

    suspend fun getTransactionByFirestoreId(firestoreId: String): CustomerTransactionEntity? {
        return customerTransactionDao.getTransactionByFirestoreId(firestoreId)
    }

    // --- Functions to handle local Sync Status (if needed directly from Repository) ---

    fun getSyncStatus(transactionId: Long): Flow<SyncStatusEntity?> {
        return syncStatusDao.getSyncStatus(transactionId)
    }

    suspend fun getSyncStatusSync(transactionId: Long): SyncStatusEntity? {
        return syncStatusDao.getSyncStatusSync(transactionId)
    }

    fun getPendingSyncTransactions(status: SyncStatus): Flow<List<SyncStatusEntity>> {
        return syncStatusDao.getSyncStatusesByStatus(status)
    }

    suspend fun getPendingSyncTransactionsSync(status: SyncStatus): List<SyncStatusEntity> {
        return syncStatusDao.getSyncStatusesByStatusSync(status)
    }

    suspend fun getTransactionByTransactionId(transactionId: Long): CustomerTransactionEntity? {
        return customerTransactionDao.getTransactionByTransactionId(transactionId)
    }
}