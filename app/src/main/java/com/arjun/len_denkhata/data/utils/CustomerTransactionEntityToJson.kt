package com.arjun.len_denkhata.data.utils

import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.google.gson.Gson

// Serialize to JSON
fun CustomerTransactionEntity.toJson(): String {
    return Gson().toJson(this)
}

// Deserialize from JSON
fun String?.toCustomerTransactionEntity(): CustomerTransactionEntity? {
    return if (this.isNullOrEmpty()) {
        null
    } else {
        Gson().fromJson(this, CustomerTransactionEntity::class.java)
    }
}