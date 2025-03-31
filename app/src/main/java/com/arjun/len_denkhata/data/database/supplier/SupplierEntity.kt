package com.arjun.len_denkhata.data.database.supplier

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    val supplierId: String,
    val name: String,
    val phone: String,
    val address: String? = null,
    val balance: Double = 0.0,
    val firebaseDocumentId: String? = null
)