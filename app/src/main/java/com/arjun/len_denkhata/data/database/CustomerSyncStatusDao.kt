package com.arjun.len_denkhata.data.database

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerSyncStatusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(syncStatus: SyncStatusEntity)

    @Update
    suspend fun update(syncStatus: SyncStatusEntity)

    @Query("SELECT * FROM syncStatus WHERE transactionId = :transactionId")
    fun getSyncStatus(transactionId: Long): Flow<SyncStatusEntity?>

    @Query("SELECT * FROM syncStatus WHERE transactionId = :transactionId")
    suspend fun getSyncStatusSync(transactionId: Long): SyncStatusEntity?

    @Query("SELECT * FROM syncStatus WHERE syncStatus = :status")
    fun getSyncStatusesByStatus(status: SyncStatus): Flow<List<SyncStatusEntity>>

    @Query("SELECT * FROM syncStatus WHERE syncStatus = :status")
    suspend fun getSyncStatusesByStatusSync(status: SyncStatus): List<SyncStatusEntity>

    @Query("SELECT * FROM syncStatus WHERE isUploaded = 0")
    fun getUnuploadedSyncStatuses(): List<SyncStatusEntity>

    @Query("SELECT * FROM syncStatus WHERE isUploaded = 0")
    suspend fun getUnuploadedSyncStatusesSync(): List<SyncStatusEntity>

    @Query("DELETE FROM syncStatus WHERE transactionId = :transactionId")
    suspend fun delete(transactionId: Long)

    @Transaction
    suspend fun markAsUploaded(transactionId: Long) {
        val currentStatus = getSyncStatusSync(transactionId)
        currentStatus?.let {
            update(it.copy(syncStatus = SyncStatus.UPLOADED, isUploaded = true))
        }
    }

    @Transaction
    suspend fun markAsPendingUpdate(transactionId: Long) {
        val currentStatus = getSyncStatusSync(transactionId)
        currentStatus?.let {
            if (it.isUploaded) {
                update(it.copy(syncStatus = SyncStatus.PENDING_UPDATE))
            }
            // If not uploaded yet, and an update occurs, we still want to upload it first.
            // The sync logic will handle this as a PENDING_UPLOAD.
        } ?: run {
            // Handle the case where there's no sync status for this transaction.
            // This might indicate an error or a race condition.
            Log.w("SyncStatusDao", "Attempted to mark non-existent transaction ($transactionId) for update.")
        }
    }

    @Transaction
    suspend fun markAsPendingDelete(transactionId: Long) {
        insert(SyncStatusEntity(transactionId = transactionId, syncStatus = SyncStatus.PENDING_DELETE, isUploaded = getSyncStatusSync(transactionId)?.isUploaded ?: false))
    }

    @Transaction
    suspend fun removeSyncStatus(transactionId: Long) {
        delete(transactionId)
    }

    @Query("""
        SELECT EXISTS(SELECT 1 FROM syncStatus WHERE
        syncStatus = 'PENDING_UPLOAD' OR
        syncStatus = 'PENDING_UPDATE' OR
        syncStatus = 'PENDING_DELETE')
    """)
    suspend fun hasPendingSync(): Boolean
}