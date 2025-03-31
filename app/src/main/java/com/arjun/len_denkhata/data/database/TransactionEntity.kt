package com.arjun.len_denkhata.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.arjun.len_denkhata.data.database.customer.CustomerEntity
import java.util.Date

@Entity(tableName = "transactions",
    foreignKeys = [
        ForeignKey(entity = CustomerEntity::class, parentColumns = ["id"], childColumns = ["customerId"], onDelete = ForeignKey.CASCADE)
    ])
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: String,
    val supplierId: String,
    val amount: Double,
    val date: Date,
    val description: String? = null,
    val isCredit: Boolean, // true for credit, false for debit
    val firebaseDocumentId: String? = null // Store Firebase document ID
)