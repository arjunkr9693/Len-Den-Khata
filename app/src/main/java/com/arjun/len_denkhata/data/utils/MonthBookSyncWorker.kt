// data/utils/MonthBookSyncWorker.kt
package com.arjun.len_denkhata.data.utils

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.arjun.len_denkhata.data.database.MonthBookSyncStatus
import com.arjun.len_denkhata.data.database.MonthBookSyncStatusDao
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionDao
import com.arjun.len_denkhata.fireStoreMonthBookTransactionPath
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

@HiltWorker
class MonthBookSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestore: FirebaseFirestore,
    private val monthBookTransactionDao: MonthBookTransactionDao,
    private val monthBookSyncStatusDao: MonthBookSyncStatusDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            Log.d("MonthBookSyncWorker", "Worker started")

            val allProcessed = handlePendingSyncs()

            Log.d("MonthBookSyncWorker", "Worker finished processing all pending sync operations. Success: $allProcessed")
            return@coroutineScope if (allProcessed) Result.success() else Result.retry()

        } catch (e: Exception) {
            Log.e("MonthBookSyncWorker", "Worker failed critically", e)
            Result.retry()
        }
    }

    private suspend fun handlePendingSyncs(): Boolean = coroutineScope {
        val uploadDeferred = async { handlePendingUploads() }
        val updateDeferred = async { handlePendingUpdates() }
        val deleteDeferred = async { handlePendingDeletes() }

        awaitAll(uploadDeferred, updateDeferred, deleteDeferred)

        return@coroutineScope !monthBookSyncStatusDao.hasPendingSync()
    }

    private suspend fun handlePendingUploads() {
        val pendingUploads = monthBookSyncStatusDao.getUnuploadedSyncStatuses()

        if (pendingUploads.isNotEmpty()) {
            Log.d("MonthBookSyncWorker", "Found ${pendingUploads.size} transactions pending upload")
            for (status in pendingUploads) {
                try {
                    val transaction = monthBookTransactionDao.getTransactionByIdOnce(status.transactionId)
                        ?: continue

                    var firestoreId: String? = null
                    firestore.collection(fireStoreMonthBookTransactionPath)
                        .add(transaction).addOnSuccessListener {
                            Log.d("MonthBookSyncWorker", "Successfully uploaded transaction ${transaction.id} (Firestore ID: ${it.id})")
                            firestoreId = it.id
                        }
                        .await()

                    firestoreId?.let{
                        monthBookTransactionDao.updateTransaction(transaction.copy(firebaseId = firestoreId))
                        monthBookSyncStatusDao.markAsUploaded(status.transactionId)
                        Log.d("MonthBookSyncWorker", "Successfully uploaded transaction ${transaction.id}")
                    }

                } catch (e: Exception) {
                    Log.e("MonthBookSyncWorker", "Failed to upload transaction ${status.transactionId}", e)
                    // Keep the status as PENDING_UPLOAD for retry
                }
            }
        }
    }

    private suspend fun handlePendingUpdates() {
        val pendingUpdates = monthBookSyncStatusDao.getSyncStatusesByStatusSync(MonthBookSyncStatus.PENDING_UPDATE)
        if (pendingUpdates.isNotEmpty()) {
            Log.d("MonthBookSyncWorker", "Found ${pendingUpdates.size} transactions pending update")
            for (status in pendingUpdates) {
                try {
                    val transaction = monthBookTransactionDao.getTransactionByIdOnce(status.transactionId)
                        ?: continue

                    val syncInfo = monthBookSyncStatusDao.getSyncStatusSync(status.transactionId)

                    var isUpdated = false
                    if (syncInfo?.isUploaded == true) {
                        firestore.collection(fireStoreMonthBookTransactionPath)
                            .document(transaction.firebaseId!!) // Assuming transaction.id can be used as Firestore doc ID if set after upload
                            .update(
                                "amount", transaction.amount,
                                "description", transaction.description,
                                "type", transaction.type.name,
                                "expenseCategory", transaction.expenseCategory?.name,
                                "edited", transaction.edited,
                                "editedOn", transaction.editedOn
                            ).addOnSuccessListener {
                                isUpdated = true
                            }
                            .await()

                        if (isUpdated) {
                            monthBookSyncStatusDao.markAsUploaded(status.transactionId)
                            Log.d("MonthBookSyncWorker", "Successfully updated transaction ${transaction.id}")
                        }
                    } else {
                        Log.w(
                            "MonthBookSyncWorker",
                            "Skipping update for transaction ${transaction.id} as it hasn't been uploaded yet. It will be uploaded first."
                        )
                    }

                } catch (e: Exception) {
                    Log.e("MonthBookSyncWorker", "Failed to update transaction ${status.transactionId}", e)
                    // Keep the status as PENDING_UPDATE for retry
                }
            }
        }
    }

    private suspend fun handlePendingDeletes() {
        val pendingDeletes = monthBookSyncStatusDao.getSyncStatusesByStatusSync(MonthBookSyncStatus.PENDING_DELETE)
        if (pendingDeletes.isNotEmpty()) {
            Log.d("MonthBookSyncWorker", "Found ${pendingDeletes.size} transactions pending delete")
            for (status in pendingDeletes) {
                try {
                    val syncInfo = monthBookSyncStatusDao.getSyncStatusSync(status.transactionId)
                    val firestoreId = monthBookTransactionDao.getTransactionByIdOnce(status.transactionId)?.firebaseId

                    var isDeleted = false
                    if (syncInfo?.isUploaded == true && firestoreId != null) {
                        firestore.collection(fireStoreMonthBookTransactionPath)
                            .document(firestoreId) // Assuming transaction.id can be used as Firestore doc ID
                            .delete()
                            .addOnSuccessListener {
                                isDeleted = true
                            }
                            .await()

                    } else {
                        Log.w(
                            "MonthBookSyncWorker",
                            "Skipping delete for transaction ${status.transactionId} as it hasn't been uploaded yet. No remote action needed."
                        )
                        monthBookSyncStatusDao.removeSyncStatus(status.transactionId)
                    }

                    if (isDeleted) {
                        monthBookSyncStatusDao.removeSyncStatus(status.transactionId)
                        monthBookTransactionDao.deleteTransactionById(status.transactionId)
                        Log.d("MonthBookSyncWorker", "Successfully deleted transaction ${status.transactionId}")
                    }

                } catch (e: Exception) {
                    Log.e("MonthBookSyncWorker", "Failed to delete transaction ${status.transactionId}", e)
                    // Keep the status as PENDING_DELETE for retry
                }
            }
        }
    }
}