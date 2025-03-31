package com.arjun.len_denkhata.data.repository.customer

import android.util.Log
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionDao
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.arjun.len_denkhata.data.database.UploadStatusDao
import com.arjun.len_denkhata.data.database.UploadStatusEntity
import com.arjun.len_denkhata.data.database.customer.CustomerDao
import com.arjun.len_denkhata.data.utils.UploadManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

class CustomerTransactionRepository @Inject constructor(
    private val customerTransactionDao: CustomerTransactionDao,
    private val firestore: FirebaseFirestore,
    private val uploadManager: UploadManager,
    private val uploadStatusDao: UploadStatusDao,
    private val customerRepository: CustomerRepository
) {

    private val _allTransactions = MutableStateFlow<List<CustomerTransactionEntity>>(emptyList())
    val allTransactions: StateFlow<List<CustomerTransactionEntity>> = _allTransactions

    private val _todayDue = MutableStateFlow(0.0)
    val todayDue: StateFlow<Double> = _todayDue

    suspend fun fetchAllTransactions(ownerId: String) {
        withContext(Dispatchers.IO) {
            customerTransactionDao.getTransactionsByOwnerId().collectLatest {
                _allTransactions.value = it
                calculateTodayDue()
            }
        }
    }

    suspend fun insertCustomerTransaction(transaction: CustomerTransactionEntity) {
        try {
            // Update cached transactions after insert
            _allTransactions.value += transaction
            val transactionId = customerTransactionDao.insert(transaction)
            customerRepository.updateCustomerBalance(transaction.customerId, transaction.amount, transaction.isCredit)
            if (transaction.isMadeByOwner) {
                uploadStatusDao.insertUploadStatus(UploadStatusEntity(transactionId = transactionId))
                uploadManager.addTransactionToUploadQueue(transaction.copy(id = transactionId))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error
        }
    }

    private suspend fun calculateTodayDue() = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()

        // Calculate the start of today (00:00:00.000)
        val todayStart = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Calculate the end of today (23:59:59.999)
        val todayEnd = calendar.apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Filter transactions for today
        val todayTransactions = _allTransactions.value.filter {
            it.timestamp in todayStart until todayEnd
        }

        // Calculate total due for today (non-credit transactions)
        val totalTodayDue = todayTransactions
            .filter { !it.isCredit }
            .sumOf { it.amount }

        Log.d("testTag", "Today due: $totalTodayDue")
        _todayDue.value = totalTodayDue
    }

    fun getCustomerTransactionsByCustomerId(customerId: String): Flow<List<CustomerTransactionEntity>> {
        return customerTransactionDao.getTransactionsByCustomerId(customerId)
//        return _allTransactions.map { transactions ->
//            transactions.filter { it.customerId == customerId || it.ownerId == customerId }
//        }
    }

    suspend fun deleteTransaction(transaction: CustomerTransactionEntity) {
        withContext(Dispatchers.IO) {
            customerTransactionDao.delete(transaction)
            customerRepository.updateCustomerBalance(transaction.customerId, transaction.amount, isCredit = !transaction.isCredit)
        }
    }
    suspend fun editTransaction(transaction: CustomerTransactionEntity) {
        withContext(Dispatchers.IO) {
            customerTransactionDao.update(transaction)
        }
    }

    suspend fun getTransactionByFirestoreId(firestoreId: String): CustomerTransactionEntity? {
        return customerTransactionDao.getTransactionByFirestoreId(firestoreId)
    }
}