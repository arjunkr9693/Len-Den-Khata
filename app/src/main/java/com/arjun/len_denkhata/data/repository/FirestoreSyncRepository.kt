package com.arjun.len_denkhata.data.repository

import android.util.Log
import com.arjun.len_denkhata.data.database.FirestoreTransaction
import com.arjun.len_denkhata.data.database.SyncStatusDao
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionDao
import com.arjun.len_denkhata.data.utils.NotificationHelper
import com.arjun.len_denkhata.data.utils.TransactionMapper
import com.arjun.len_denkhata.data.utils.TransactionProcessor
import com.arjun.len_denkhata.fireStoreCustomerTransactionPath
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val transactionDao: CustomerTransactionDao,
    private val syncStatusDao: SyncStatusDao,
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
    private var ownerTransactionListener: ListenerRegistration? = null

    // Start listening for incoming transactions where current user is the customer
    fun startIncomingTransactionListener(currentUserId: String) {
        stopListening()
        Log.d("FirestoreSync", "Starting incoming transaction listener for user (customer) $currentUserId")
        customerListener = firestore.collection(fireStoreCustomerTransactionPath)
            .whereEqualTo("customerId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreSync", "Incoming (customer) listen failed", error)
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    when (change.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.d("Incoming Firebase (customer)", "Received new transaction: ${change.document.data}")
                            externalScope.launch { processIncomingTransaction(change.document) }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            Log.d("Incoming Firebase (customer)", "Modified transaction: ${change.document.data}")
                            externalScope.launch { processIncomingTransaction(change.document, isUpdate = true) }
                        }
                        DocumentChange.Type.REMOVED -> {
                            Log.d("Incoming Firebase (customer)", "Removed transaction: ${change.document.data}")
                            externalScope.launch { processRemovedTransaction(change.document) }
                        }
                    }
                }
            }
    }

    private suspend fun processIncomingTransaction(document: DocumentSnapshot, isUpdate: Boolean = false) {
        try {
            val firestoreTransaction = document.toObject(FirestoreTransaction::class.java)
                ?: return

            Log.d("FirestoreSync", "${if (isUpdate) "Updating" else "Processing new"} incoming transaction: $firestoreTransaction")
            transactionProcessor.processIncomingTransaction(
                firestoreId = document.id,
                ownerId = firestoreTransaction.ownerId,
                customerId = firestoreTransaction.customerId,
                amount = firestoreTransaction.amount,
                date = firestoreTransaction.date,
                isCredit = firestoreTransaction.credit,
                description = firestoreTransaction.description,
                timestamp = firestoreTransaction.timestamp,
                isEdited = firestoreTransaction.isEdited,
                editedOn = firestoreTransaction.editedOn,
                isUpdate = isUpdate
            )

        } catch (e: Exception) {
            Log.e("FirestoreSync", "Deserialization error during processIncomingTransaction", e)
        }
    }

    private suspend fun processRemovedTransaction(document: DocumentSnapshot) {
        try {
            val firestoreId = document.id
            Log.d("FirestoreSync", "Processing removed transaction with Firestore ID: $firestoreId")
            transactionProcessor.processRemovedTransaction(firestoreId)
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error processing removed transaction", e)
        }
    }

    fun stopListening() {
        customerListener?.remove()
        customerListener = null
        ownerTransactionListener?.remove()
        ownerTransactionListener = null
        Log.d("FirestoreSync", "Stopped all Firestore listeners")
    }
}