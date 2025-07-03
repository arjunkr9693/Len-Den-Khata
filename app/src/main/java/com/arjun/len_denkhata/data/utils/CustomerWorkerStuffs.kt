package com.arjun.len_denkhata.data.utils

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.arjun.len_denkhata.data.database.CustomerSyncStatusDao
import com.arjun.len_denkhata.data.database.MonthBookSyncStatusDao
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionDao
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionDao
import com.arjun.len_denkhata.data.utils.worker.CustomerSyncWorker
import com.arjun.len_denkhata.data.utils.worker.MonthBookSyncWorker
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

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

fun enqueueMonthBookUploadWorker(@ApplicationContext context: Context) {
    val uploadWorkRequest = OneTimeWorkRequestBuilder<MonthBookSyncWorker>()
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
        "monthBookSync",
        ExistingWorkPolicy.KEEP,
        uploadWorkRequest
    )
}

class CustomWorkerFactory @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val customerTransactionDao: CustomerTransactionDao,
    private val customerSyncStatusDao: CustomerSyncStatusDao,
    private val monthBookTransactionDao: MonthBookTransactionDao,
    private val monthBookSyncStatusDao: MonthBookSyncStatusDao
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            CustomerSyncWorker::class.java.name ->
                CustomerSyncWorker(
                    appContext,
                    workerParameters,
                    firestore,
                    customerTransactionDao,
                    customerSyncStatusDao
                )
            MonthBookSyncWorker::class.java.name ->
                MonthBookSyncWorker(
                    appContext,
                    workerParameters,
                    firestore,
                    monthBookTransactionDao,
                    monthBookSyncStatusDao
                )
            else ->
                null // Return null if the worker class is unknown
        }
    }
}