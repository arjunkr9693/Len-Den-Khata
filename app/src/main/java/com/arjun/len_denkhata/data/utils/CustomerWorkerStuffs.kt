package com.arjun.len_denkhata.data.utils

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.arjun.len_denkhata.data.database.SyncStatusDao
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionDao
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject



//private fun checkAndEnqueueUploadWorker(applicationScope: CoroutineScope, context: Context) {
//    applicationScope.launch {
//        try {
//            val hasPendingSync = withContext(Dispatchers.IO) {
//                syncStatusDao.hasPendingSync()
//            }
//            Log.d("UploadCheck", "status: $hasPendingSync")
//            if (hasPendingSync) {
//                Log.d("UploadCheck", "Found unuploaded data, enqueuing worker")
//                enqueueUploadWorker(context)
//            } else {
//                Log.d("UploadCheck", "No unuploaded data found")
//            }
//        } catch (e: Exception) {
//            Log.e("UploadCheck", "Error checking for unuploaded data", e)
//            // Enqueue worker anyway to be safe
//            enqueueUploadWorker(context)
//        }
//    }
//}

fun enqueueUploadWorker(@ApplicationContext context: Context) {
    val uploadWorkRequest = OneTimeWorkRequestBuilder<CustomerSyncWorker>()
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .setBackoffCriteria(
            BackoffPolicy.LINEAR, // Use linear backoff
            10,                  // Backoff delay
            TimeUnit.SECONDS       // Time unit for the delay
        )
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "customerSync",
        ExistingWorkPolicy.KEEP,
        uploadWorkRequest
    )
}

class CustomWorkerFactory @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val customerTransactionDao: CustomerTransactionDao,
    private val syncStatusDao: SyncStatusDao
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return when (workerClassName) {
            CustomerSyncWorker::class.java.name ->
                CustomerSyncWorker(appContext, workerParameters, firestore, customerTransactionDao, syncStatusDao)
            else ->
                throw IllegalArgumentException("Unknown worker class: $workerClassName")
        }
    }
}