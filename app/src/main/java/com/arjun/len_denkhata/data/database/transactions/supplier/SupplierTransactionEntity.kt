package com.arjun.len_denkhata.data.database.transactions.supplier

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.arjun.len_denkhata.data.database.supplier.SupplierEntity
import java.util.Date

@Entity(tableName = "supplierTransactions",
    foreignKeys = [
        ForeignKey(entity = SupplierEntity::class, parentColumns = ["id"], childColumns = ["supplierId"], onDelete = ForeignKey.CASCADE)
    ])
data class SupplierTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val date: Date,
    val paidAmount: Double,
    val firebaseDocumentId: String? = null
)