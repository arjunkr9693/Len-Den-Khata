package com.arjun.len_denkhata.data.database.customer

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(customer: CustomerEntity): Long

    @Transaction
    @Update
    suspend fun update(customer: CustomerEntity)

    @Transaction
    @Delete
    suspend fun delete(customer: CustomerEntity)

    @Query("SELECT * FROM customers")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE phone = :phone")
    fun getCustomerByPhone(phone: String): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE id = :customerId")
    suspend fun getCustomerByIdSync(customerId: String): CustomerEntity?

    @Transaction
    @Query("UPDATE customers SET overallBalance = :balance WHERE id = :customerId")
    suspend fun updateCustomerBalance(customerId: String, balance: Double)

    @Query("SELECT * FROM customers WHERE id = :customerId")
    fun getCustomerById(customerId: String): Flow<CustomerEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM customers WHERE id = :customerId LIMIT 1)")
    suspend fun customerExists(customerId: String): Boolean
}