package com.arjun.len_denkhata.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.arjun.len_denkhata.data.database.customer.CustomerDao
import com.arjun.len_denkhata.data.database.customer.CustomerEntity
import com.arjun.len_denkhata.data.database.supplier.SupplierDao
import com.arjun.len_denkhata.data.database.supplier.SupplierEntity
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionDao
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionEntity
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionDao

@Database(entities = [CustomerEntity::class, SupplierEntity::class, TransactionEntity::class, CustomerTransactionEntity::class, SyncStatusEntity::class, MonthBookTransactionEntity::class, MonthBookSyncStatusEntity::class], version = 1)
@TypeConverters(Converters::class) // Create a Converters class for Date
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun transactionDao(): TransactionDao
    abstract fun customerTransactionDao(): CustomerTransactionDao
    abstract fun uploadStatusDao(): CustomerSyncStatusDao
    abstract fun monthBookTransactionDao(): MonthBookTransactionDao
    abstract fun monthBookSyncStatusDao(): MonthBookSyncStatusDao
}