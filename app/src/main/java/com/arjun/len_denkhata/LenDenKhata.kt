package com.arjun.len_denkhata

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.work.*
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionDao
import com.arjun.len_denkhata.data.database.SyncStatusDao
import com.arjun.len_denkhata.data.repository.FirestoreSyncRepository
import com.arjun.len_denkhata.data.utils.CustomWorkerFactory
import com.arjun.len_denkhata.data.utils.CustomerSyncWorker
import com.arjun.len_denkhata.data.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltAndroidApp
class LenDenKhata : Application(), Configuration.Provider{

    @Inject
    lateinit var workerFactory: CustomWorkerFactory

    @Inject lateinit var syncRepository: FirestoreSyncRepository

    private var phoneNumberJob: Job? = null

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        setupLifecycleObservers()
    }

    private fun setupLifecycleObservers() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                logActivityEvent("created", activity)
            }

            override fun onActivityStarted(activity: Activity) {
                logActivityEvent("started", activity)
            }

            override fun onActivityResumed(activity: Activity) {
                UserSession.isContactPickerShowing = false

                // Start observing phone number
                phoneNumberJob = applicationScope.launch {
                    UserSession.observablePhoneNumber
                        .filterNotNull()
                        .take(1)
                        .collect { userId ->
                            Log.d("OwnerID", userId)
                            syncRepository.startIncomingTransactionListener(userId)
                            // Automatically cancels after collection
                        }
                }

                logActivityEvent("resumed", activity)
            }

            override fun onActivityPaused(activity: Activity) {
                phoneNumberJob?.cancel()
                phoneNumberJob = null

                if (!UserSession.isContactPickerShowing) {
                    logActivityEvent("paused", activity)
                }
            }

            override fun onActivityStopped(activity: Activity) {
                syncRepository.stopListening()
                logActivityEvent("stopped", activity)
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                logActivityEvent("saveInstanceState", activity)
            }

            override fun onActivityDestroyed(activity: Activity) {
                logActivityEvent("destroyed", activity)
            }
        })
    }


    private fun logActivityEvent(event: String, activity: Activity) {
        Log.d("AppLifecycle", "${activity.localClassName} $event")
    }



    override fun onTerminate() {
        applicationScope.cancel()
        super.onTerminate()
    }
}

