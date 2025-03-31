package com.arjun.len_denkhata.data.database.transactions.customer

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.arjun.len_denkhata.data.database.customer.CustomerEntity
import java.util.Date

@Entity(
    tableName = "customerTransactions",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
)
data class CustomerTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firestoreId: String? = null,
    val ownerId: String,
    val customerId: String,
    val amount: Double,
    val date: Date,
    val description: String? = null,
    val isCredit: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)