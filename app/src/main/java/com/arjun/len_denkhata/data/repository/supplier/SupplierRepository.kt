package com.arjun.len_denkhata.data.repository.supplier

import com.arjun.len_denkhata.data.database.supplier.SupplierDao
import com.arjun.len_denkhata.data.database.supplier.SupplierEntity
import com.arjun.len_denkhata.data.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class SupplierRepository @Inject constructor(
    private val supplierDao: SupplierDao,
    private val firestore: FirebaseFirestore,
) {
    suspend fun insertSupplier(supplier: SupplierEntity): Long {
        val firebaseDocumentId = supplier.phone;

        return try {
            firestore.collection("suppliers").document(firebaseDocumentId).set(supplier.copy(firebaseDocumentId = firebaseDocumentId)).await()
            val roomSupplier = supplier.copy(firebaseDocumentId = firebaseDocumentId)
            supplierDao.insert(roomSupplier)
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }
    suspend fun addSupplier(supplier: SupplierEntity) {
        val firebaseDocumentId = UUID.randomUUID().toString()
        val firebaseSupplier = supplier.copy(firebaseDocumentId = firebaseDocumentId)

        try {
            firestore.collection("suppliers").document(firebaseDocumentId).set(firebaseSupplier).await()
            val roomSupplier = SupplierEntity(
                id = UserSession.phoneNumber!!,
                supplierId = firebaseSupplier.id.toString(),
                name = supplier.name,
                phone = supplier.phone,
                address = supplier.address,
                balance = supplier.balance,
                firebaseDocumentId = firebaseDocumentId
            )
            supplierDao.insert(roomSupplier)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error
        }
    }


    fun getAllSuppliers(): Flow<List<SupplierEntity>> {
        return supplierDao.getAllSuppliers()
    }

}