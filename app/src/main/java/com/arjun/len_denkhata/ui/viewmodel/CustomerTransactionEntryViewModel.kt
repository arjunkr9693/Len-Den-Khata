package com.arjun.len_denkhata.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.arjun.len_denkhata.data.repository.customer.CustomerRepository
import com.arjun.len_denkhata.data.repository.customer.CustomerTransactionRepository
import com.arjun.len_denkhata.data.utils.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CustomerTransactionEntryViewModel @Inject constructor(
    private val transactionRepository: CustomerTransactionRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    val _transactionForEdit = MutableStateFlow<CustomerTransactionEntity?>(null)
    val transactionForEdit = _transactionForEdit.asStateFlow()

    fun saveTransaction(
        customerId: String,
        amount: Double,
        description: String,
        isCredit: Boolean,
        date: Date,
        navController: NavHostController,
    ) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()

            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)
            val millisecond = calendar.get(Calendar.MILLISECOND)

            val dateCalendar = Calendar.getInstance()
            dateCalendar.time = date

            dateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            dateCalendar.set(Calendar.MINUTE, minute)
            dateCalendar.set(Calendar.SECOND, second)
            dateCalendar.set(Calendar.MILLISECOND, millisecond)

            val mergedTimestamp = dateCalendar.timeInMillis

            val transaction = CustomerTransactionEntity(
                ownerId = UserSession.phoneNumber!!,
                customerId = customerId,
                amount = amount,
                date = date,
                description = description,
                isCredit = isCredit,
                timestamp = mergedTimestamp
            )

            Log.d("DateCheck", "Date: $date")
            transactionRepository.insertCustomerTransaction(transaction)

        }
        navController.popBackStack() // Go back to transaction list
    }
    fun updateTransaction(
        originalAmount: Double,
        amount: Double,
        description: String,
        customerTransaction: CustomerTransactionEntity,
        navController: NavHostController,
        date: Date // The coming date (only date part is relevant)
    ) {
        viewModelScope.launch {
            val originalTimestampCalendar = Calendar.getInstance()
            originalTimestampCalendar.timeInMillis = customerTransaction.timestamp

            val hourOfDay = originalTimestampCalendar.get(Calendar.HOUR_OF_DAY)
            val minute = originalTimestampCalendar.get(Calendar.MINUTE)
            val second = originalTimestampCalendar.get(Calendar.SECOND)
            val millisecond = originalTimestampCalendar.get(Calendar.MILLISECOND)

            val newDateCalendar = Calendar.getInstance()
            newDateCalendar.time = date

            newDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            newDateCalendar.set(Calendar.MINUTE, minute)
            newDateCalendar.set(Calendar.SECOND, second)
            newDateCalendar.set(Calendar.MILLISECOND, millisecond)

            val mergedTimestamp = newDateCalendar.timeInMillis

            val updatedTransaction = customerTransaction.copy(
                amount = amount,
                description = description,
                isEdited = true,
                date = date, // Keep the incoming date in the date field
                timestamp = mergedTimestamp, // Save the merged timestamp
                editedOn = System.currentTimeMillis()
            )
            transactionRepository.updateTransaction(updatedTransaction, originalAmount)
            // customerRepository.updateCustomerBalance(updatedTransaction.customerId, amount - customerTransaction.amount, isCredit = customerTransaction.isCredit)
        }
        navController.popBackStack() // Go back to transaction list
    }

    suspend fun loadTransactionByTransactionId(transactionId: Long) {
        viewModelScope.launch {
            _transactionForEdit.value = transactionRepository.getTransactionByTransactionId(transactionId)
        }

    }
}