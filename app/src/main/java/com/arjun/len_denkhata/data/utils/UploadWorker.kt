package com.arjun.len_denkhata.data.utils

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionDao
import com.arjun.len_denkhata.data.database.UploadStatusDao
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestore: FirebaseFirestore,
    private val customerTransactionDao: CustomerTransactionDao,
    private val uploadStatusDao: UploadStatusDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            Log.d("UploadWorker", "Worker started")

            // Get unuploaded transactions
            val unUploadedStatuses = uploadStatusDao.getUnuploadedTransactions()
            if (unUploadedStatuses.isNullOrEmpty()) {
                return@coroutineScope Result.success()
            }

            Log.d("UploadWorker", "Found ${unUploadedStatuses.size} transactions to upload")

            // Process each transaction sequentially
            for (status in unUploadedStatuses) {
                try {
                    val transaction = customerTransactionDao.getTransactionById(status.transactionId)
                        ?: continue  // Skip if transaction not found

                    // Upload with await() instead of callback
                    firestore.collection("customerTransactions")
                        .add(transaction)
                        .await()  // This suspends until completion

                    // Only mark as uploaded if successful
                    uploadStatusDao.deleteUploadStatus(status)
                    Log.d("UploadWorker", "Successfully uploaded transaction ${transaction.id}")

                } catch (e: Exception) {
                    Log.e("UploadWorker", "Failed to upload transaction ${status.transactionId}", e)
                    // Continue with next transaction instead of failing immediately
                    // The worker will be retried later for remaining transactions
                }
            }

            // Check if all were successful
            val remaining = uploadStatusDao.getUnuploadedTransactions()?.size
            return@coroutineScope if (remaining == 0) {
                Result.success()
            } else {
                Log.d("UploadWorker", "$remaining transactions failed, will retry")
                Result.retry()
            }

        } catch (e: Exception) {
            Log.e("UploadWorker", "Worker failed critically", e)
            Result.retry()
        }
    }
}