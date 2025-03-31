package com.arjun.len_denkhata.data.database.transactions.supplier

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: SupplierTransactionEntity)

    @Query("SELECT * FROM supplierTransactions WHERE supplierId = :supplierId")
    fun getTransactionsBySupplierId(supplierId: Long): Flow<List<SupplierTransactionEntity>>

    @Query("SELECT * FROM supplierTransactions WHERE firebaseDocumentId = :firebaseId")
    fun getTransactionByFirebaseId(firebaseId: String): Flow<SupplierTransactionEntity?>
}