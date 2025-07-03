package com.arjun.len_denkhata.di

import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import com.arjun.len_denkhata.data.database.AppDatabase
import com.arjun.len_denkhata.data.database.customer.CustomerDao
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionDao
import com.arjun.len_denkhata.data.database.supplier.SupplierDao
import com.arjun.len_denkhata.data.database.TransactionDao
import com.arjun.len_denkhata.data.database.CustomerSyncStatusDao
import com.arjun.len_denkhata.data.database.MonthBookSyncStatusDao
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionDao
import com.arjun.len_denkhata.data.repository.customer.CustomerRepository
import com.arjun.len_denkhata.data.repository.customer.CustomerTransactionRepository
import com.arjun.len_denkhata.data.utils.TransactionMapper
import com.arjun.len_denkhata.data.utils.TransactionProcessor
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MonthBookPreferences

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CustomerPreferences

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "khatabook_db"
        ).addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideCustomerDao(appDatabase: AppDatabase): CustomerDao {
        return appDatabase.customerDao()
    }

    @Provides
    fun provideSupplierDao(appDatabase: AppDatabase): SupplierDao {
        return appDatabase.supplierDao()
    }

    @Provides
    fun provideTransactionDao(appDatabase: AppDatabase): TransactionDao {
        return appDatabase.transactionDao()
    }

    @Provides
    fun provideUploadStatusDao(appDatabase: AppDatabase): CustomerSyncStatusDao {
        return appDatabase.uploadStatusDao()
    }

    @Provides
    fun provideCustomerTransactionDao(database: AppDatabase): CustomerTransactionDao {
        return database.customerTransactionDao()
    }

    @Provides
    fun provideMonthBookTransactionDao(database: AppDatabase): MonthBookTransactionDao {
        return database.monthBookTransactionDao()
    }

    @Provides
    fun provideMonthBookSyncStatusDao(database: AppDatabase): MonthBookSyncStatusDao {
        return database.monthBookSyncStatusDao()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManagerCompat {
        return NotificationManagerCompat.from(context)
    }

    @Provides
    fun provideTransactionMapper(): TransactionMapper = TransactionMapper()

    @Provides
    @Singleton
    @MonthBookPreferences
    fun provideMonthBookSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("monthbook_preferences", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    @CustomerPreferences
    fun provideCustomerSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("customer_preferences", Context.MODE_PRIVATE)
    }

    @Provides
    fun provideTransactionProcessor(
        customerTransactionRepository: CustomerTransactionRepository,
        customerRepository: CustomerRepository
    ): TransactionProcessor {
        return TransactionProcessor(customerTransactionRepository, customerRepository)
    }
}