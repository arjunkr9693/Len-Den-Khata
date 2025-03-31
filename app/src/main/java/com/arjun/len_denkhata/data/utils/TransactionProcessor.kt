package com.arjun.len_denkhata.data.utils

import android.util.Log
import androidx.room.Transaction
import com.arjun.len_denkhata.data.database.customer.CustomerDao
import com.arjun.len_denkhata.data.database.customer.CustomerEntity
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionDao
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
        ownerId: String,  // The customer ID from Firestore
        customerId: String,
        amount: Double,
        date: Date,
        isCredit: Boolean,
        description: String = ""
    ) {
        // 1. Ensure owner exists as customer
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

        val test = !isCredit
        Log.d("TransactionProcessor", "Processing incoming transaction with isCredit: {$test}")
        withContext(Dispatchers.IO) {
            if (customerTransactionRepository.getTransactionByFirestoreId(firestoreId) == null) {
                customerTransactionRepository.insertCustomerTransaction(
                    CustomerTransactionEntity(
                        firestoreId = firestoreId,
                        ownerId = customerId,
                        customerId = ownerId,
                        amount = amount,
                        date = date,
                        isCredit = !isCredit,
                        description = description,
                        isMadeByOwner = false
                    )
                )
                Log.d("Room Insert", "Success")
            }
        }
    }
}