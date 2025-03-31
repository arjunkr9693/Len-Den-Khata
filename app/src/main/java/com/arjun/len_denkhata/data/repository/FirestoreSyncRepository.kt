package com.arjun.len_denkhata.data.repository

import android.util.Log
import com.arjun.len_denkhata.data.database.UploadStatusDao
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionDao
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.arjun.len_denkhata.data.utils.NotificationHelper
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val transactionDao: CustomerTransactionDao,
    private val uploadStatusDao: UploadStatusDao,
    private val notificationHelper: NotificationHelper,
) {
    @Inject
    lateinit var externalScope: CoroutineScope

    private var customerListener: ListenerRegistration? = null

    // Start listening for incoming transactions where current user is the customer
    fun startIncomingTransactionListener(currentUserId: String) {
        stopListening()

        customerListener = firestore.collection("customerTransactions")
            .whereEqualTo("customerId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreSync", "Listen failed", error)
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        Log.d("Incoming Firebase", "Received new transaction: ${change.document.data}")
                        externalScope.launch { processIncomingTransaction(change.document)}
                    }
                }
            }
    }

    private suspend fun processIncomingTransaction(document: DocumentSnapshot) {
        val remoteTransaction = document.toObject(CustomerTransactionEntity::class.java)
            ?.copy(firestoreId = document.id)
            ?: return

        withContext(Dispatchers.IO) {
            // Check if we already have this transaction
            val existing = transactionDao.getTransactionByFirestoreId(document.id)

            if (existing == null) {
                // Insert new incoming transaction
                transactionDao.insert(remoteTransaction)
                notificationHelper.showTransactionNotification(remoteTransaction)
            }
        }
    }

    // Sync local transactions to Firestore
    suspend fun syncOutgoingTransactions() {
        val unuploaded = uploadStatusDao.getUnuploadedTransactions() ?: return

        unuploaded.forEach { uploadStatus ->
            try {
                val transaction = transactionDao.getTransactionById(uploadStatus.transactionId)
                    ?: return@forEach

                // Upload to Firestore
                val docRef = firestore.collection("customerTransactions")
                    .add(transaction)
                    .await()

                // Update status
                uploadStatusDao.deleteUploadStatus(uploadStatus)

                // Update transaction with Firestore ID
                transactionDao.update(
                    transaction.copy(firestoreId = docRef.id)
                )

            } catch (e: Exception) {
                Log.e("FirestoreSync", "Failed to upload transaction ${uploadStatus.transactionId}", e)
            }
        }
    }

    fun stopListening() {
        customerListener?.remove()
        customerListener = null
    }
}