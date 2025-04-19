package com.arjun.len_denkhata.data.database.transactions.customer

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: CustomerTransactionEntity): Long

    @Update
    suspend fun update(transaction: CustomerTransactionEntity)

    @Delete
    suspend fun delete(transaction: CustomerTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMultipleTransactions(transactions: List<CustomerTransactionEntity>)

    @Query("SELECT * FROM customerTransactions WHERE customerId = :customerId AND isDeleted = 0 ORDER BY timestamp DESC")
    fun getTransactionsByCustomerId(customerId: String): Flow<List<CustomerTransactionEntity>>

    @Query("SELECT * FROM customerTransactions WHERE id = :transactionId")
    fun getTransactionById(transactionId: Long): CustomerTransactionEntity?

    @Query("SELECT * FROM customerTransactions")
    fun getTransactionsByOwnerId(): Flow<List<CustomerTransactionEntity>>

    @Query("SELECT * FROM customerTransactions WHERE firestoreId = :firestoreId")
    suspend fun getTransactionByFirestoreId(firestoreId: String): CustomerTransactionEntity?

    @Query("DELETE FROM customerTransactions WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: Long)

    @Query("SELECT * FROM customerTransactions WHERE timestamp >= :startTime AND timestamp <= :endTime AND isCredit = 0 AND isDeleted = 0")
    fun getTodayDebitTransactions(startTime: Long, endTime: Long): Flow<List<CustomerTransactionEntity>>

}