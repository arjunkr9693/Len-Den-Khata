package com.arjun.len_denkhata.data.repository.customer

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
    suspend fun addCustomer(customer: CustomerEntity) {
        val firebaseDocumentId = UUID.randomUUID().toString()
        val firebaseCustomer = customer.copy()

        try {
            firestore.collection("customers").document(firebaseDocumentId).set(firebaseCustomer).await()
            val roomCustomer = CustomerEntity(
                id = customer.id.toString(),
                name = customer.name,
                phone = customer.phone,
                overallBalance = customer.overallBalance,
            )
            customerDao.insert(roomCustomer)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error
        }
    }

//    suspend fun syncCustomersWithFirebase() {
//        try {
//            val snapshot = firestore.collection("customers").get().await()
//            snapshot.documents.forEach { document ->
//                val firebaseCustomer = document.toObject<Customer>()
//                firebaseCustomer?.let {
//                    val localCustomer = customerDao.getCustomerByFirebaseId(it.firebaseDocumentId ?: "").firstOrNull()
//
//                    if (localCustomer == null) {
//                        val roomCustomer = CustomerEntity(
//                            id = it.id,
//                            name = it.name,
//                            phone = it.phone,
//                            address = it.address,
//                            balance = it.balance,
//                            firebaseDocumentId = it.firebaseDocumentId
//                        )
//                        customerDao.insert(roomCustomer)
//                    } else {
////                        // Update local customer if needed
////                        val updatedRoomCustomer = localCustomer.firstOrNull()?.copy(
////                            name = it.name,
////                            phone = it.phone,
////                            address = it.address,
////                            balance = it.balance
////                        )
////                        if (updatedRoomCustomer != null){
////                            customerDao.update(updatedRoomCustomer)
////                        }
//                    }
//                }
//            }
//            // Add code to upload local changes to firebase.
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    fun getAllCustomers(): Flow<List<CustomerEntity>> {
        return customerDao.getAllCustomers()
    }


    suspend fun insertCustomer(customer: CustomerEntity): Long {

        return try {
            customerDao.insert(customer)
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

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
}