package com.arjun.len_denkhata.data.repository.customer

import androidx.room.Transaction
import com.arjun.len_denkhata.data.database.customer.CustomerDao
import com.arjun.len_denkhata.data.database.customer.CustomerEntity
import com.arjun.len_denkhata.data.repository.LoginRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao,
    private val firestore: FirebaseFirestore,
    private val loginRepository: LoginRepository
) {

    fun getAllCustomers(): Flow<List<CustomerEntity>> {
        return customerDao.getAllCustomers()
    }

    @Transaction
    suspend fun insertCustomer(customer: CustomerEntity): Long {
        return try {
            customerDao.insert(customer)
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    @Transaction
    suspend fun updateCustomerBalance(customerId: String, amount: Double, isCredit: Boolean, isEditing: Boolean = false) = withContext(
        Dispatchers.IO) {
        val customer = customerDao.getCustomerByIdSync(customerId) ?: return@withContext
        val newBalance = if (isCredit) customer.overallBalance - amount else customer.overallBalance + amount
        customerDao.updateCustomerBalance(customerId, newBalance)
    }

    suspend fun calculateHaveToGive(customers: List<CustomerEntity>): Double = withContext(Dispatchers.IO) {
        var haveToGive = 0.0
        for (customer in customers) {
            if(customer.overallBalance < 0)
                haveToGive += customer.overallBalance
        }
        return@withContext haveToGive
    }

    suspend fun calculateWillGet(customers: List<CustomerEntity>): Double = withContext(Dispatchers.IO) {
        var haveToGive = 0.0
        for (customer in customers) {
            if(customer.overallBalance > 0)
                haveToGive += customer.overallBalance
        }
        return@withContext haveToGive
    }

    fun getCustomerById(customerId: String): Flow<CustomerEntity?> {
        return customerDao.getCustomerById(customerId)
    }

    suspend fun deleteCustomer(customerEntity: CustomerEntity) {
        customerDao.delete(customerEntity)
    }

    suspend fun updateCustomer(updatedCustomer: CustomerEntity) {
        customerDao.update(updatedCustomer)
    }

    suspend fun customerExists(ownerId: String): Boolean {
        return customerDao.customerExists(ownerId)
    }

    // New method to update customer with last updated timestamp
//    suspend fun updateCustomerWithTimestamp(updatedCustomer: CustomerEntity) {
////        val customerWithTimestamp = updatedCustomer.copy(lastUpdated = System.currentTimeMillis())
//        customerDao.update()
//    }
}