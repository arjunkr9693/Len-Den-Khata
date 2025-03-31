package com.arjun.len_denkhata.data.utils

import com.arjun.len_denkhata.data.database.FirestoreTransaction
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity

class TransactionMapper {
    fun toRoomEntity(firestoreTransaction: FirestoreTransaction): CustomerTransactionEntity {
        return CustomerTransactionEntity(
            id = 0, // Let Room auto-generate
            ownerId = firestoreTransaction.ownerId,
            customerId = firestoreTransaction.customerId,
            amount = firestoreTransaction.amount,
            date = firestoreTransaction.date,
            description = firestoreTransaction.description,
            isCredit = firestoreTransaction.credit,
            timestamp = firestoreTransaction.timestamp
        )
    }
    
//    // Add reverse mapping if needed
//    fun toFirestoreDto(roomEntity: CustomerTransactionEntity): FirestoreTransaction {
//        return FirestoreTransaction(
//            ownerId = roomEntity.ownerId,
//            customerId = roomEntity.customerId,
//            // ... other fields
//        )
//    }
}