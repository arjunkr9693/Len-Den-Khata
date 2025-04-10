package com.arjun.len_denkhata.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.len_denkhata.data.database.customer.CustomerDao
import com.arjun.len_denkhata.data.database.customer.CustomerEntity
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookExpenseCategory
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionEntity
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionType
import com.arjun.len_denkhata.data.repository.MonthBookRepository
import com.arjun.len_denkhata.data.repository.customer.CustomerRepository
import com.arjun.len_denkhata.data.repository.customer.CustomerTransactionRepository
import com.arjun.len_denkhata.data.utils.UserSession
import com.arjun.len_denkhata.fireStoreCustomerTransactionPath
import com.arjun.len_denkhata.fireStoreMonthBookTransactionPath
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

enum class InitialLoadingState {
    LOADING,
    LOADED,
    ERROR
}

@HiltViewModel
class InitialDataLoaderViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val customerTransactionRepository: CustomerTransactionRepository,
    private val monthBookRepository: MonthBookRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _loadingState = mutableStateOf(InitialLoadingState.LOADING)
    val loadingState: State<InitialLoadingState> = _loadingState

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    suspend fun downloadInitialData() {
        _loadingState.value = InitialLoadingState.LOADING
        Log.d("InitialData", "Loading state set to LOADING")
        try {
            val currentOwnerId = UserSession.phoneNumber

            val customerTransactions = fetchCustomerTransactions(currentOwnerId!!)
            Log.d("InitialCustomer", customerTransactions.toString())

            val monthBookTransactions = fetchMonthBookTransactions(currentOwnerId)
            Log.d("InitialMonth", monthBookTransactions.toString())

            processAndStoreCustomerTransactions(customerTransactions, currentOwnerId)
            Log.d("InitialData", "Customer transactions processed and stored")

            processAndStoreMonthBookTransactions(monthBookTransactions)
            Log.d("InitialData", "Month book transactions processed and stored")

            _loadingState.value = InitialLoadingState.LOADED
            Log.d("InitialData", "Loading state set to LOADED")

        } catch (e: Exception) {
            _errorMessage.value = "Failed to load initial data: ${e.localizedMessage}"
            _loadingState.value = InitialLoadingState.ERROR
            Log.e("InitialData", "Error in downloadInitialData(): ${e.localizedMessage}")
        }
    }

    private suspend fun fetchCustomerTransactions(ownerId: String): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = firestore.collection(fireStoreCustomerTransactionPath)
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            snapshot.documents.map { it.data.orEmpty() + ("firestoreId" to it.id) }
        } catch (e: Exception) {
            Log.e("InitialData", "Error fetching customer transactions: ${e.localizedMessage}")
            emptyList()
        }
    }

    private suspend fun fetchMonthBookTransactions(ownerId: String): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("InitialData", "Fetching month book transactions for ownerId: $ownerId")
            val snapshot = firestore.collection(fireStoreMonthBookTransactionPath)
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            snapshot.documents.map { it.data.orEmpty() + ("firestoreId" to it.id) }
        } catch (e: Exception) {
            Log.e("InitialData", "Error fetching month book transactions: ${e.localizedMessage}")
            emptyList()
        }
    }

    private suspend fun processAndStoreCustomerTransactions(
        firestoreTransactions: List<Map<String, Any>>,
        currentOwnerId: String
    ) = withContext(Dispatchers.IO) {
        val customerTransactionEntities = mutableListOf<CustomerTransactionEntity>()
        val newCustomers = firestoreTransactions.distinctBy { it["customerId"] as? String }
            .filter { (it["customerId"] as? String) != currentOwnerId }
            .map { CustomerEntity(phone = it["customerId"] as String, id = it["customerId"] as String, name = it["customerId"] as String) }
            .toSet() // Use Set to ensure uniqueness

        newCustomers.forEach {
            customerRepository.insertCustomer(it)
        }

        firestoreTransactions.forEach { data ->
            val firestoreId = data["firestoreId"] as? String ?: return@forEach
            val ownerId = data["ownerId"] as? String ?: return@forEach
            val customerId = data["customerId"] as? String ?: return@forEach
            val amount = (data["amount"] as? Number)?.toDouble() ?: 0.0
            val dateLong = data["date"] as? Long ?: 0L
            val date = Date(dateLong)
            val isCredit = data["credit"] as? Boolean ?: false
            val description = data["description"] as? String ?: ""
            val isMadeByOwner = data["madeByOwner"] as? Boolean ?: false
            val timestamp = data["timestamp"] as? Long ?: 0L

            customerTransactionRepository.insertCustomerTransaction(
                CustomerTransactionEntity(
                    firestoreId = firestoreId,
                    ownerId = ownerId,
                    customerId = customerId,
                    amount = amount,
                    date = date,
                    isCredit = isCredit,
                    description = description,
                    isMadeByOwner = isMadeByOwner,
                    timestamp = timestamp,
                ),
                initialStoringWhenLoggin = true
            )
        }
    }

    private suspend fun processAndStoreMonthBookTransactions(
        firestoreTransactions: List<Map<String, Any>>
    ) = withContext(Dispatchers.IO) {
        val monthBookTransactionEntities = mutableListOf<MonthBookTransactionEntity>()

        for (data in firestoreTransactions) {
            val firestoreId = data["firestoreId"] as? String ?: continue
            val ownerId = data["ownerId"] as? String ?: continue
            val amount = (data["amount"] as? Number)?.toDouble() ?: 0.0
            val timestamp = data["timestamp"] as? Long ?: 0L
            val typeString = data["type"] as? String ?: ""
            val type = try {
                enumValueOf<MonthBookTransactionType>(typeString.uppercase())
            } catch (e: IllegalArgumentException) {
                Log.w("InitialData", "Unknown MonthBookTransaction type: $typeString")
                continue
            }
            val description = data["description"] as? String ?: ""
            val monthBookExpenseCategoryString = data["monthBookExpenseCategory"] as? String
            val monthBookExpenseCategory = monthBookExpenseCategoryString?.let {
                try {
                    enumValueOf<MonthBookExpenseCategory>(it.uppercase())
                } catch (e: IllegalArgumentException) {
                    Log.w("InitialData", "Unknown MonthBookExpenseCategory: $it")
                    null
                }
            }

            monthBookRepository.insertTransaction(
                MonthBookTransactionEntity(
                    id = 0, // Room will auto-generate the ID
                    firebaseId = firestoreId,
                    ownerId = ownerId,
                    amount = amount,
                    timestamp = timestamp,
                    type = type,
                    description = description,
                    expenseCategory = monthBookExpenseCategory
                )
            )
        }
    }
}