package com.arjun.len_denkhata.data.utils

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.arjun.len_denkhata.data.database.SyncStatus
import com.arjun.len_denkhata.data.database.CustomerSyncStatusDao
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionDao
import com.arjun.len_denkhata.fireStoreCustomerTransactionPath
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

@HiltWorker
class CustomerSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestore: FirebaseFirestore,
    private val customerTransactionDao: CustomerTransactionDao,
    private val syncStatusDao: CustomerSyncStatusDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            Log.d("CustomerSyncWorker", "Worker started")

            val allProcessed = handlePendingSyncs()

            Log.d("CustomerSyncWorker", "Worker finished processing all pending sync operations. Success: $allProcessed")
            return@coroutineScope if (allProcessed) Result.success() else Result.retry()
//            return@coroutineScope Result.success()

        } catch (e: Exception) {
            Log.e("CustomerSyncWorker", "Worker failed critically", e)
            Result.retry()
        }
    }

    private suspend fun handlePendingSyncs(): Boolean = coroutineScope {
        val uploadDeferred = async { handlePendingUploads() }
        val updateDeferred = async { handlePendingUpdates() }
        val deleteDeferred = async { handlePendingDeletes() }

        awaitAll(uploadDeferred, updateDeferred, deleteDeferred)

        // After all three have completed, check if there are any pending syncs
        return@coroutineScope !syncStatusDao.hasPendingSync()
    }

    private suspend fun handlePendingUploads() {
        val pendingUploads = syncStatusDao.getUnuploadedSyncStatuses()

        if (pendingUploads.isNotEmpty()) {
            Log.d("CustomerSyncWorker", "Found ${pendingUploads.size} transactions pending upload")
            for (status in pendingUploads) {
                try {
                    val transaction = customerTransactionDao.getTransactionByTransactionId(status.transactionId)
                        ?: continue

                    var firestoreId: String? = null
                    firestore.collection(fireStoreCustomerTransactionPath)
                        .add(transaction).addOnSuccessListener {
                            firestoreId = it.id
                            Log.d("CustomerSyncWorker", "Successfully uploaded transaction ${transaction.id} (Firestore ID: ${it.id})")
                        }
                        .await()

                    if(firestoreId != null) {
                        customerTransactionDao.update(transaction.copy(firestoreId = firestoreId))
                        syncStatusDao.markAsUploaded(status.transactionId)
                        Log.d("CustomerSyncWorker", "Successfully uploaded transaction ${transaction.id}")
                    }

                } catch (e: Exception) {
                    Log.e("CustomerSyncWorker", "Failed to upload transaction ${status.transactionId}", e)
                    // Keep the status as PENDING_UPLOAD for retry
                }
            }
        }
    }


    private suspend fun handlePendingUpdates(){
        val pendingUpdates = syncStatusDao.getSyncStatusesByStatusSync(SyncStatus.PENDING_UPDATE)
        if (pendingUpdates.isNotEmpty()) {
            Log.d("CustomerSyncWorker", "Found ${pendingUpdates.size} transactions pending update")
            for (status in pendingUpdates) {
                try {
                    val transaction = customerTransactionDao.getTransactionByTransactionId(status.transactionId)
                        ?: continue

                    val syncInfo = syncStatusDao.getSyncStatusSync(status.transactionId)
                    if (syncInfo?.isUploaded == true) {
                        firestore.collection(fireStoreCustomerTransactionPath)
                            .document(transaction.firestoreId.toString())
                            .update("amount", transaction.amount, "description", transaction.description, "edited", transaction.isEdited, "editedOn", transaction.editedOn)
                            .await()

                        syncStatusDao.markAsUploaded(status.transactionId)
                        Log.d("CustomerSyncWorker", "Successfully updated transaction ${transaction.id}")
                    } else {
                        Log.w(
                            "CustomerSyncWorker",
                            "Skipping update for transaction ${transaction.id} as it hasn't been uploaded yet. It will be uploaded first."
                        )
                    }

                } catch (e: Exception) {
                    Log.e("CustomerSyncWorker", "Failed to update transaction ${status.transactionId}", e)
                    // Keep the status as PENDING_UPDATE for retry
                }
            }
        }
    }

    private suspend fun handlePendingDeletes(){
        val pendingDeletes = syncStatusDao.getSyncStatusesByStatusSync(SyncStatus.PENDING_DELETE)
        if (pendingDeletes.isNotEmpty()) {
            Log.d("CustomerSyncWorker", "Found ${pendingDeletes.size} transactions pending delete")
            for (status in pendingDeletes) {
                try {
                    val syncInfo = syncStatusDao.getSyncStatusSync(status.transactionId)
                    val transaction = customerTransactionDao.getTransactionByTransactionId(status.transactionId)

                    if (syncInfo?.isUploaded == true) {
                        transaction?.let {
                            firestore.collection(fireStoreCustomerTransactionPath)
                                .document(it.firestoreId!!)
                                .delete()
                                .await()

                            syncStatusDao.markAsUploaded(status.transactionId)
                            customerTransactionDao.deleteTransactionById(status.transactionId)
                            Log.d("CustomerSyncWorker", "Successfully deleted transaction ${status.transactionId}")
                        }
                    } else {
                        Log.w(
                            "CustomerSyncWorker",
                            "Skipping delete for transaction ${status.transactionId} as it hasn't been uploaded yet. No remote action needed."
                        )
                        syncStatusDao.removeSyncStatus(status.transactionId)
                    }

                } catch (e: Exception) {
                    Log.e("CustomerSyncWorker", "Failed to delete transaction ${status.transactionId}", e)
                    // Keep the status as PENDING_DELETE for retry
                }
            }
        }
    }

}