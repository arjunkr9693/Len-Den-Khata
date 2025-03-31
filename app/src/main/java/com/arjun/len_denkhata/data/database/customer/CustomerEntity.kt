package com.arjun.len_denkhata.data.database.customer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    val name: String,
    val phone: String,
    val overallBalance: Double = 0.0,
)