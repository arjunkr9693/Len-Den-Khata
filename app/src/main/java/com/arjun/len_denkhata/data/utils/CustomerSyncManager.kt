package com.arjun.len_denkhata.data.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.arjun.len_denkhata.data.database.SyncStatus
import com.arjun.len_denkhata.data.database.CustomerSyncStatusDao
import com.arjun.len_denkhata.data.database.SyncStatusEntity
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionDao
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.arjun.len_denkhata.fireStoreCustomerTransactionPath
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val customerTransactionDao: CustomerTransactionDao,
    private val syncStatusDao: CustomerSyncStatusDao,
    @ApplicationContext private val applicationContext: Context,
    private val coroutineScope: CoroutineScope,
    private val applicationScope: CoroutineScope
) {

    private var isNetworkAvailable = NetworkUtils.isInternetAvailable(applicationContext)

    init {
        Log.d("SyncManager", "SyncManager initialized")
        registerNetworkCallback()
        // No initial sync on start, wait for actions or network availability changes
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
                    Log.d("SyncManager", "Network available.")
                    // Worker will handle pending tasks when it runs
                }
            }

            override fun onLost(network: Network) {
                coroutineScope.launch {
                    isNetworkAvailable = false
                    Log.d("SyncManager", "Network lost.")
                    // No need to immediately enqueue here, the individual enqueue functions will handle it
                }
            }
        }
        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        } catch (e: SecurityException) {
            Log.e("SyncManager", "Failed to register network callback: ${e.message}")
        }
    }

    // --- Public Functions to Handle Data Changes ---

    fun enqueueTransactionForUpload(transaction: CustomerTransactionEntity) {
        coroutineScope.launch(Dispatchers.IO) {
            syncStatusDao.insert(SyncStatusEntity(transactionId = transaction.id, syncStatus = SyncStatus.PENDING_UPLOAD, isUploaded = false))
            if (isNetworkAvailable) {
                performUpload(transaction)
            } else {
                enqueueUploadWorker(applicationContext)
            }
        }
    }

    fun enqueueTransactionForUpdate(transaction: CustomerTransactionEntity) {
        coroutineScope.launch(Dispatchers.IO) {
            val existingStatus = syncStatusDao.getSyncStatusSync(transaction.id)
            if (existingStatus == null) {
                syncStatusDao.insert(SyncStatusEntity(transactionId = transaction.id, syncStatus = SyncStatus.PENDING_UPLOAD, isUploaded = false))
            } else if (existingStatus.syncStatus != SyncStatus.PENDING_DELETE) {
                syncStatusDao.update(existingStatus.copy(syncStatus = SyncStatus.PENDING_UPDATE))
            }
            if (isNetworkAvailable) {
                performUpdate(transaction)
            } else {
                enqueueUploadWorker(applicationContext)
            }
        }
    }

    fun enqueueTransactionForDelete(transactionId: Long) {
        coroutineScope.launch(Dispatchers.IO) {
            val existingStatus = syncStatusDao.getSyncStatusSync(transactionId)
            if (existingStatus == null || existingStatus.syncStatus != SyncStatus.PENDING_UPLOAD) {
                syncStatusDao.insert(SyncStatusEntity(transactionId = transactionId, syncStatus = SyncStatus.PENDING_DELETE, isUploaded = existingStatus?.isUploaded ?: false))
            } else {
                syncStatusDao.removeSyncStatus(transactionId)
                Log.d("SyncManager", "Delete requested for not-yet-uploaded transaction $transactionId. Removed from pending uploads.")
                return@launch // No need to sync or enqueue worker
            }
            if (isNetworkAvailable) {
                performDelete(transactionId)
            } else {
                enqueueUploadWorker(applicationContext)
            }
        }
    }

    // --- Internal Functions to Handle Direct Operations ---

    private suspend fun performUpload(transaction: CustomerTransactionEntity) {
        try {
            var firebaseId: String? = null
            firestore.collection(fireStoreCustomerTransactionPath)
                .add(transaction)
                .addOnSuccessListener {
                    firebaseId = it.id
                    Log.d("SyncManager", "Successfully uploaded transaction ${transaction.id} (Firestore ID: $firebaseId)")
                }
                .await()

            firebaseId?.let {
                customerTransactionDao.update(transaction.copy(firestoreId = firebaseId))
            }
            syncStatusDao.update(SyncStatusEntity(transactionId = transaction.id, syncStatus = SyncStatus.UPLOADED, isUploaded = true))
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to upload transaction ${transaction.id}", e)
            // Keep the PENDING_UPLOAD status for the worker to handle
        }
    }

    private suspend fun performUpdate(transaction: CustomerTransactionEntity) {
        try {
            val firestoreId = getFirestoreDocumentId(transaction)
            firestoreId?.let {
                firestore.collection(fireStoreCustomerTransactionPath)
                    .document(it)
                    .update("amount", transaction.amount, "description", transaction.description, "edited", transaction.isEdited, "editedOn", transaction.editedOn)
                    .await()
                syncStatusDao.markAsUploaded(transaction.id)
                Log.d("SyncManager", "Successfully updated transaction ${transaction.id} (Firestore ID: $it)")
            } ?: Log.w("SyncManager", "Firestore ID not found for update: ${transaction.id}")
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to update transaction ${transaction.id}", e)
            // Keep the PENDING_UPDATE status for the worker to handle
        }
    }

    private suspend fun performDelete(transactionId: Long) {
        try {
            val firestoreId = getFirestoreDocumentId(customerTransactionDao.getTransactionById(transactionId))
            firestoreId?.let {
                firestore.collection(fireStoreCustomerTransactionPath)
                    .document(it)
                    .delete()
                    .await()
                syncStatusDao.removeSyncStatus(transactionId)
                customerTransactionDao.deleteTransactionById(transactionId)
                Log.d("SyncManager", "Successfully deleted transaction ${transactionId} (Firestore ID: $it)")
            } ?: Log.w("SyncManager", "Firestore ID not found for delete: ${transactionId}")
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to delete transaction ${transactionId}", e)
            // Keep the PENDING_DELETE status for the worker to handle
        }
    }

    // --- Helper Functions ---

    private suspend fun getFirestoreDocumentId(transaction: CustomerTransactionEntity?): String? {
        if (transaction == null) return null
        return try {
            val querySnapshot = firestore.collection(fireStoreCustomerTransactionPath)
                .whereEqualTo("ownerId", transaction.ownerId)
                .whereEqualTo("id", transaction.id)
                .whereEqualTo("timestamp", transaction.timestamp)
                .get()
                .await()
            if (!querySnapshot.isEmpty) {
                querySnapshot.documents[0].id
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Error getting Firestore document ID: ${e.message}")
            null
        }
    }}