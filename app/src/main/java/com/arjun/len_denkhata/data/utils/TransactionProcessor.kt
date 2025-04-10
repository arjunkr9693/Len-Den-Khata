package com.arjun.len_denkhata.data.utils

import android.util.Log
import androidx.room.Transaction
import com.arjun.len_denkhata.data.database.customer.CustomerEntity
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.arjun.len_denkhata.data.repository.customer.CustomerRepository
import com.arjun.len_denkhata.data.repository.customer.CustomerTransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionProcessor @Inject constructor(
    private val customerTransactionRepository: CustomerTransactionRepository,
    private val customerRepository: CustomerRepository
) {
    @Transaction
    suspend fun processIncomingTransaction(
        firestoreId: String,
        ownerId: String,  // The customer ID from Firestore (who initiated the transaction)
        customerId: String, // The current user's ID (who is the recipient)
        amount: Double,
        date: Date,
        isCredit: Boolean,
        description: String = "",
        timestamp: Long,
        isEdited: Boolean = false,
        editedOn: Long? = null,
        isUpdate: Boolean = false
    ) {
        // 1. Ensure owner exists as customer in local DB
        if (!customerRepository.customerExists(ownerId)) {
            customerRepository.insertCustomer(
                CustomerEntity(
                    id = ownerId,
                    name = ownerId, // Default name
                    phone = ownerId,
                    overallBalance = 0.0,
                )
            )
        }

        withContext(Dispatchers.IO) {
            val existingTransaction = customerTransactionRepository.getTransactionByFirestoreId(firestoreId)

            if (existingTransaction == null) {
                // Insert new incoming transaction
                customerTransactionRepository.insertCustomerTransaction(
                    CustomerTransactionEntity(
                        firestoreId = firestoreId,
                        ownerId = customerId, // For incoming, current user is the 'owner' in local context
                        customerId = ownerId, // The initiator is the 'customer' in this transaction
                        amount = amount,
                        date = date,
                        isCredit = !isCredit, // Inverted for the recipient
                        description = description,
                        isMadeByOwner = false,
                        timestamp = timestamp,
                    )
                )
                Log.d("Room Insert (Incoming)", "Success for Firestore ID: $firestoreId")
            } else if (isUpdate && existingTransaction.editedOn != editedOn) {
                // Update existing incoming transaction if modified remotely
                customerTransactionRepository.updateTransaction(
                    CustomerTransactionEntity(
                        id = existingTransaction.id, // Keep the local ID
                        firestoreId = firestoreId,
                        ownerId = customerId,
                        customerId = ownerId,
                        amount = amount,
                        date = date,
                        isCredit = !isCredit,
                        description = description,
                        isMadeByOwner = false,
                        timestamp = timestamp,
                        isEdited = isEdited,
                        editedOn = editedOn
                    ),
                    existingTransaction.amount
                )
                Log.d("Room Update (Incoming)", "Success for Firestore ID: $firestoreId")
            } else {

            }
        }
    }

    @Transaction
    suspend fun processRemovedTransaction(firestoreId: String) {
        withContext(Dispatchers.IO) {
            val existingTransaction = customerTransactionRepository.getTransactionByFirestoreId(firestoreId)
            if (existingTransaction != null) {
                customerTransactionRepository.deleteTransaction(existingTransaction)
                Log.d("Room Delete (Incoming)", "Success for Firestore ID: $firestoreId")
            } else {
                Log.d("Room Delete (Incoming)", "Transaction with Firestore ID $firestoreId not found locally.")
            }
        }
    }
}