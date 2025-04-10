// data/database/MonthBookSyncStatusDao.kt
package com.arjun.len_denkhata.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthBookSyncStatusDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(syncStatus: MonthBookSyncStatusEntity)

    @Update
    suspend fun update(syncStatus: MonthBookSyncStatusEntity)

    @Query("SELECT * FROM month_book_sync_status WHERE transactionId = :transactionId")
    fun getSyncStatus(transactionId: Long): Flow<MonthBookSyncStatusEntity?>

    @Query("SELECT * FROM month_book_sync_status WHERE transactionId = :transactionId")
    suspend fun getSyncStatusSync(transactionId: Long): MonthBookSyncStatusEntity?

    @Query("SELECT * FROM month_book_sync_status WHERE syncStatus = :status")
    suspend fun getSyncStatusesByStatusSync(status: MonthBookSyncStatus): List<MonthBookSyncStatusEntity>

    @Query("SELECT * FROM month_book_sync_status WHERE isUploaded = 0 AND syncStatus != :deleting")
    suspend fun getUnuploadedSyncStatuses(deleting: MonthBookSyncStatus = MonthBookSyncStatus.PENDING_DELETE): List<MonthBookSyncStatusEntity>

    @Query("UPDATE month_book_sync_status SET isUploaded = 1, syncStatus = :uploaded WHERE transactionId = :transactionId")
    suspend fun markAsUploaded(transactionId: Long, uploaded: MonthBookSyncStatus = MonthBookSyncStatus.UPLOADED)

    @Query("DELETE FROM month_book_sync_status WHERE transactionId = :transactionId")
    suspend fun removeSyncStatus(transactionId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM month_book_sync_status WHERE syncStatus != :uploaded)")
    suspend fun hasPendingSync(uploaded: MonthBookSyncStatus = MonthBookSyncStatus.UPLOADED): Boolean
}