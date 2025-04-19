// data/utils/MonthBookSyncManager.kt
package com.arjun.len_denkhata.data.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.arjun.len_denkhata.data.database.MonthBookSyncStatus
import com.arjun.len_denkhata.data.database.MonthBookSyncStatusDao
import com.arjun.len_denkhata.data.database.MonthBookSyncStatusEntity
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionDao
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionEntity
import com.arjun.len_denkhata.fireStoreMonthBookTransactionPath
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonthBookSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val monthBookTransactionDao: MonthBookTransactionDao,
    private val monthBookSyncStatusDao: MonthBookSyncStatusDao,
    @ApplicationContext private val applicationContext: Context,
    private val coroutineScope: CoroutineScope,
) {

    private var isNetworkAvailable = NetworkUtils.isInternetAvailable(applicationContext)

    init {
        Log.d("MonthBookSyncManager", "MonthBookSyncManager initialized")
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                coroutineScope.launch {
                    isNetworkAvailable = true
                    Log.d("MonthBookSyncManager", "Network available.")
                    enqueueMonthBookUploadWorker(applicationContext)
                }
            }

            override fun onLost(network: Network) {
                coroutineScope.launch {
                    isNetworkAvailable = false
                    Log.d("MonthBookSyncManager", "Network lost.")
                    // No immediate action, enqueuing is done on demand
                }
            }
        }
        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        } catch (e: SecurityException) {
            Log.e("MonthBookSyncManager", "Failed to register network callback: ${e.message}")
        }
    }

    fun enqueueTransactionForUpload(transaction: MonthBookTransactionEntity) {
        coroutineScope.launch(Dispatchers.IO) {
            monthBookSyncStatusDao.insert(MonthBookSyncStatusEntity(transactionId = transaction.id, syncStatus = MonthBookSyncStatus.PENDING_UPLOAD, isUploaded = false))
            if (isNetworkAvailable) {
                performUpload(transaction)
            } else {
                enqueueMonthBookUploadWorker(applicationContext)
            }
        }
    }

    fun enqueueTransactionForUpdate(
        transaction: MonthBookTransactionEntity,
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val existingStatus = monthBookSyncStatusDao.getSyncStatusSync(transaction.id)
            if (existingStatus == null) {
                monthBookSyncStatusDao.insert(MonthBookSyncStatusEntity(transactionId = transaction.id, syncStatus = MonthBookSyncStatus.PENDING_UPLOAD, isUploaded = false))
            } else if (existingStatus.syncStatus != MonthBookSyncStatus.PENDING_DELETE) {
                monthBookSyncStatusDao.update(existingStatus.copy(syncStatus = MonthBookSyncStatus.PENDING_UPDATE))
            }
            if (isNetworkAvailable) {
                performUpdate(transaction)
            } else {
                enqueueMonthBookUploadWorker(applicationContext)
            }
        }
    }

    fun enqueueTransactionForDelete(transactionId: Long) {
        coroutineScope.launch(Dispatchers.IO) {
            val existingStatus = monthBookSyncStatusDao.getSyncStatusSync(transactionId)
            if (existingStatus == null || existingStatus.syncStatus != MonthBookSyncStatus.PENDING_UPLOAD) {
                monthBookSyncStatusDao.insert(MonthBookSyncStatusEntity(transactionId = transactionId, syncStatus = MonthBookSyncStatus.PENDING_DELETE, isUploaded = existingStatus?.isUploaded ?: false))
            } else {
                monthBookSyncStatusDao.removeSyncStatus(transactionId)
                Log.d("MonthBookSyncManager", "Delete requested for not-yet-uploaded transaction $transactionId. Removed from pending uploads.")
                return@launch
            }
            if (isNetworkAvailable) {
                performDelete(transactionId)
            } else {
                enqueueMonthBookUploadWorker(applicationContext)
            }
        }
    }

    private suspend fun performUpload(transaction: MonthBookTransactionEntity) {
        try {
            var firebaseId: String? = null
            firestore.collection(fireStoreMonthBookTransactionPath)
                .add(transaction)
                .addOnSuccessListener {
                    firebaseId = it.id
                    Log.d("MonthBookSyncManager", "Successfully uploaded transaction ${transaction.id} (Firestore ID: $firebaseId)")
                }
                .await()

            firebaseId?.let {
                monthBookTransactionDao.updateTransaction(transaction.copy(firebaseId = firebaseId))
                monthBookSyncStatusDao.update(MonthBookSyncStatusEntity(transactionId = transaction.id, syncStatus = MonthBookSyncStatus.UPLOADED, isUploaded = true))
            }
        } catch (e: Exception) {
            Log.e("MonthBookSyncManager", "Failed to upload transaction ${transaction.id}", e)
            // Keep the PENDING_UPLOAD status for the worker to handle
        }
    }

    private suspend fun performUpdate(transaction: MonthBookTransactionEntity) {
        try {
            val firestoreId = transaction.firebaseId
            firestoreId?.let {
                firestore.collection(fireStoreMonthBookTransactionPath)
                    .document(it)
                    .update(
                        "amount", transaction.amount,
                        "description", transaction.description,
                        "type", transaction.type.name,
                        "expenseCategory", transaction.expenseCategory?.name,
                        "timestamp", transaction.timestamp,
                        "edited", transaction.edited,
                        "editedOn", transaction.editedOn
                    )
                    .await()
                monthBookSyncStatusDao.markAsUploaded(transaction.id)
                Log.d("MonthBookSyncManager", "Successfully updated transaction ${transaction.id} (Firestore ID: $it)")
            } ?: Log.w("MonthBookSyncManager", "Firestore ID not found for update: ${transaction.id}")
        } catch (e: Exception) {
            Log.e("MonthBookSyncManager", "Failed to update transaction ${transaction.id}", e)
            // Keep the PENDING_UPDATE status for the worker to handle
        }
    }

    private suspend fun performDelete(transactionId: Long) {
        try {
            val firestoreId = monthBookTransactionDao.getTransactionByIdOnce(transactionId)?.firebaseId
            firestoreId?.let {
                firestore.collection(fireStoreMonthBookTransactionPath)
                    .document(it)
                    .delete()
                    .await()
                monthBookSyncStatusDao.removeSyncStatus(transactionId)
                monthBookTransactionDao.deleteTransactionById(transactionId)
                Log.d("MonthBookSyncManager", "Successfully deleted transaction $transactionId (Firestore ID: $it)")
            } ?: Log.w("MonthBookSyncManager", "Firestore ID not found for delete: $transactionId")
        } catch (e: Exception) {
            Log.e("MonthBookSyncManager", "Failed to delete transaction $transactionId", e)
            // Keep the PENDING_DELETE status for the worker to handle
        }
    }
}