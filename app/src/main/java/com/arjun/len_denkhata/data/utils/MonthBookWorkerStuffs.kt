package com.arjun.len_denkhata.data.utils

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.arjun.len_denkhata.data.database.MonthBookSyncStatusDao
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionDao
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject



//class CustomWorkerFactory @Inject constructor(
//    private val firestore: FirebaseFirestore,
//    private val customerTransactionDao: CustomerTransactionDao,
//    private val customerSyncStatusDao: CustomerSyncStatusDao,
//    private val monthBookTransactionDao: MonthBookTransactionDao,
//    private val monthBookSyncStatusDao: MonthBookSyncStatusDao
//) : WorkerFactory() {
//
//    override fun createWorker(
//        appContext: Context,
//        workerClassName: String,
//        workerParameters: WorkerParameters
//    ): ListenableWorker? {
//        return when (workerClassName) {
//            CustomerSyncWorker::class.java.name ->
//                CustomerSyncWorker(appContext, workerParameters, firestore, customerTransactionDao, customerSyncStatusDao)
//            MonthBookSyncWorker::class.java.name ->
//                MonthBookSyncWorker(appContext, workerParameters, firestore, monthBookTransactionDao, monthBookSyncStatusDao)
//            else ->
//                null // Return null if the worker class is unknown
//        }
//    }
//}