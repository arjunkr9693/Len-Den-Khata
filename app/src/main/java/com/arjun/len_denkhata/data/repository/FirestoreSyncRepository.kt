package com.arjun.len_denkhata.data.repository

import android.util.Log
import com.arjun.len_denkhata.data.database.FirestoreTransaction
import com.arjun.len_denkhata.data.database.UploadStatusDao
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionDao
import com.arjun.len_denkhata.data.utils.NotificationHelper
import com.arjun.len_denkhata.data.utils.TransactionMapper
import com.arjun.len_denkhata.data.utils.TransactionProcessor
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val transactionDao: CustomerTransactionDao,
    private val uploadStatusDao: UploadStatusDao,
    private val notificationHelper: NotificationHelper,
    private val mapper: TransactionMapper,
    private val transactionProcessor: TransactionProcessor
) {
    init {
        Log.d("FirestoreSync", "Repository initialized")
    }
    @Inject
    lateinit var externalScope: CoroutineScope

    private var customerListener: ListenerRegistration? = null

    // Start listening for incoming transactions where current user is the customer
    fun startIncomingTransactionListener(currentUserId: String) {
        stopListening()
        Log.d("FirestoreSync", "Starting incoming transaction listener for user $currentUserId")
        customerListener = firestore.collection("customerTransactions")
            .whereEqualTo("customerId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreSync", "Listen failed", error)
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    when(change.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.d("Incoming Firebase", "Received new transaction: ${change.document.data}")
                            externalScope.launch { processIncomingTransaction(change.document)}
                        }
                        DocumentChange.Type.MODIFIED -> {
                            Log.d("Incoming Firebase", "Modified transaction: ${change.document.data}")
                        }
                        DocumentChange.Type.REMOVED -> {
                            Log.d("Incoming Firebase", "Removed transaction: ${change.document.data}")
                        }
                    }
//                    if (change.type == DocumentChange.Type.ADDED) {
//                        Log.d("Incoming Firebase", "Received new transaction: ${change.document.data}")
//                        externalScope.launch { processIncomingTransaction(change.document)}
//                    }else if (change.type == DocumentChange.Type.MODIFIED) {
//                        Log.d("Incoming Firebase", "Modified transaction: ${change.document.data}")
//                    }
//                    else if (change.type == DocumentChange.Type.REMOVED) {
//                        Log.d("Incoming Firebase", "Removed transaction: ${change.document.data}")
//                    }
                }
            }
    }

    private suspend fun processIncomingTransaction(document: DocumentSnapshot) {
        try {
            val firestoreTransaction = document.toObject(FirestoreTransaction::class.java)
                ?: return

            Log.d("FirestoreSync", "Processing incoming transaction: $firestoreTransaction")
            transactionProcessor.processIncomingTransaction(
                firestoreId = document.id,
                ownerId = firestoreTransaction.ownerId,
                customerId = firestoreTransaction.customerId,
                amount = firestoreTransaction.amount,
                date = firestoreTransaction.date,
                isCredit = firestoreTransaction.credit,
                description = firestoreTransaction.description,
            )

//            val roomEntity = mapper.toRoomEntity(firestoreTransaction)
//                .copy(firestoreId = document.id).also {
//                    Log.d("Room Conversion", "Success")
//                }

//            withContext(Dispatchers.IO) {
//                if (transactionDao.getTransactionByFirestoreId(document.id) == null) {
//                    val transactionId = transactionDao.insert(roomEntity)
//                    Log.d("Room Insert", "Success")
//                }
//            }
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Deserialization error", e)
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