package com.arjun.len_denkhata.data.database.customer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    val phone: String,
    val name: String,
    val overallBalance: Double = 0.0,
    val isNameModifiedByUser: Boolean = false, // Track if user manually changed the name
    val lastUpdated: Long = System.currentTimeMillis(), // Last updated timestamp
    val profilePictureUri: String? = null // Store contact's profile picture URI
)