package com.arjun.len_denkhata.data.database

import java.util.Date

data class FirestoreTransaction(
    val ownerId: String = "",
    val customerId: String = "",
    val amount: Double = 0.0,
    val date: Date = Date(),
    val description: String = "",
    val credit: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)