package com.arjun.len_denkhata.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete

@Dao
interface UploadStatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUploadStatus(uploadStatus: UploadStatusEntity)

    @Query("SELECT * FROM uploadStatus WHERE transactionId = :transactionId")
    suspend fun getUploadStatus(transactionId: String): UploadStatusEntity?

    @Query("SELECT * FROM uploadStatus WHERE uploadStatus = :status")
    suspend fun getUnuploadedTransactions(status: Boolean = false): List<UploadStatusEntity>?

    @Delete
    suspend fun deleteUploadStatus(uploadStatus: UploadStatusEntity)

    @Query("SELECT * FROM uploadStatus")
    suspend fun getAllUploadStatus(): List<UploadStatusEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM uploadStatus LIMIT 1)")
    suspend fun hasUnuploadedTransactions(): Boolean
}