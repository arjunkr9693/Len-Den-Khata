package com.arjun.len_denkhata.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity

enum class SyncStatus {
    PENDING_UPLOAD,
    PENDING_UPDATE,
    PENDING_DELETE,
    UPLOADED,
    FAILED // Optional: To track failed syncs
}

@Entity(
    tableName = "syncStatus",
    foreignKeys = [ForeignKey(
        entity = CustomerTransactionEntity::class,
        parentColumns = ["id"],
        childColumns = ["transactionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("transactionId")]
)
data class SyncStatusEntity(
    @PrimaryKey val transactionId: Long,
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD, // Initial state is pending upload
    val isUploaded: Boolean = false // Indicates if the initial upload was successful
)