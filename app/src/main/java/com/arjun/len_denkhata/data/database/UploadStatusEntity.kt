package com.arjun.len_denkhata.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity

@Entity(
    tableName = "uploadStatus",
    foreignKeys = [ForeignKey(
        entity = CustomerTransactionEntity::class,
        parentColumns = ["id"],
        childColumns = ["transactionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("transactionId")]
)
data class UploadStatusEntity(
    @PrimaryKey val transactionId: Long,
    val uploadStatus: Boolean = false
)