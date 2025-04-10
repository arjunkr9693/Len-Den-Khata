// data/database/MonthBookSyncStatus.kt
package com.arjun.len_denkhata.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionEntity

enum class MonthBookSyncStatus {
    PENDING_UPLOAD,
    PENDING_UPDATE,
    PENDING_DELETE,
    UPLOADED,
    FAILED
}

@Entity(
    tableName = "month_book_sync_status",
    foreignKeys = [ForeignKey(
        entity = MonthBookTransactionEntity::class,
        parentColumns = ["id"],
        childColumns = ["transactionId"],
        onDelete = ForeignKey.CASCADE // Define what happens on transaction deletion
    )]
)
data class MonthBookSyncStatusEntity(
    @PrimaryKey(autoGenerate = false)
    val transactionId: Long,
    val syncStatus: MonthBookSyncStatus,
    val isUploaded: Boolean = false
)