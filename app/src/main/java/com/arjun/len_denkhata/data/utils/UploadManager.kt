package com.arjun.len_denkhata.data.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionDao
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.arjun.len_denkhata.data.database.UploadStatusDao
import com.arjun.len_denkhata.data.database.UploadStatusEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val customerTransactionDao: CustomerTransactionDao,
    private val uploadStatusDao: UploadStatusDao,
    private val applicationContext: Context,
    private val coroutineScope: CoroutineScope
) {

    private val transactionQueue = mutableListOf<CustomerTransactionEntity>()
    private var isNetworkAvailable = NetworkUtils.isInternetAvailable(applicationContext)

    init {
        registerNetworkCallback()
        coroutineScope.launch {
            uploadPendingTransactions()
        }
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
                    uploadFromQueue()
                }
            }

            override fun onLost(network: Network) {
                coroutineScope.launch {
                    isNetworkAvailable = false
                }
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun addTransactionToUploadQueue(transaction: CustomerTransactionEntity) {
        Log.d("testTag", "came to add to transaction")
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                if (isNetworkAvailable) {
                    Log.d("testTag", "Device showing internet is available")
                    uploadTransaction(transaction)
                } else {
                    Log.d("testTag", "Device showing internet is not available")
                    transactionQueue.add(transaction)
                }
            }
        }
    }

    private suspend fun uploadTransaction(transaction: CustomerTransactionEntity) {
        try {
            firestore.collection("customerTransactions").add(transaction).addOnSuccessListener {
                coroutineScope.launch {
                    Log.d("testTag", "Direct Uploading")
                    uploadStatusDao.deleteUploadStatus(UploadStatusEntity(transaction.id))
                }

            }
        } catch (e: Exception) {
            transactionQueue.add(transaction)
        }
    }

    private suspend fun uploadFromQueue() {
        val iterator = transactionQueue.iterator()
        while (iterator.hasNext()) {
            val transaction = iterator.next()
            try {
                Log.d("testTag", "Uploading From Queue")
                uploadTransaction(transaction)
                iterator.remove()
            } catch (e: Exception) {
                break
            }
        }
    }

    private suspend fun uploadPendingTransactions() {
        val unuploadedTransactions = uploadStatusDao.getAllUploadStatus()
        unuploadedTransactions.forEach { uploadStatus ->
            val transaction = customerTransactionDao.getTransactionById(uploadStatus.transactionId) ?: return@forEach
            uploadTransaction(transaction)
        }
    }
}