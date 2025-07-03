package com.arjun.len_denkhata.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.arjun.len_denkhata.data.database.customer.CustomerDao
import com.arjun.len_denkhata.data.database.customer.CustomerEntity
import com.arjun.len_denkhata.data.database.supplier.SupplierDao
import com.arjun.len_denkhata.data.database.supplier.SupplierEntity
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionDao
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionEntity
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionDao

@Database(
    entities = [
        CustomerEntity::class,
        SupplierEntity::class,
        TransactionEntity::class,
        CustomerTransactionEntity::class,
        SyncStatusEntity::class,
        MonthBookTransactionEntity::class,
        MonthBookSyncStatusEntity::class
    ],
    version = 2 // Updated from 1 to 2
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun transactionDao(): TransactionDao
    abstract fun customerTransactionDao(): CustomerTransactionDao
    abstract fun uploadStatusDao(): CustomerSyncStatusDao
    abstract fun monthBookTransactionDao(): MonthBookTransactionDao
    abstract fun monthBookSyncStatusDao(): MonthBookSyncStatusDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to customers table
                database.execSQL(
                    "ALTER TABLE customers ADD COLUMN isNameModifiedByUser INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE customers ADD COLUMN lastUpdated INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}"
                )
                database.execSQL(
                    "ALTER TABLE customers ADD COLUMN profilePictureUri TEXT"
                )
            }
        }
    }
}