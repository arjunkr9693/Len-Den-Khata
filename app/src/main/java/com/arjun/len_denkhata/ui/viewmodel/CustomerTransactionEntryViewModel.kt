package com.arjun.len_denkhata.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.arjun.len_denkhata.data.repository.customer.CustomerRepository
import com.arjun.len_denkhata.data.repository.customer.CustomerTransactionRepository
import com.arjun.len_denkhata.data.utils.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CustomerTransactionEntryViewModel @Inject constructor(
    private val transactionRepository: CustomerTransactionRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

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
        date: Date // Added date parameter
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

            val mergedEditedOnTimestamp = dateCalendar.timeInMillis

            val updatedTransaction = customerTransaction.copy(
                amount = amount,
                description = description,
                isEdited = true,
                date = date,
                timestamp = mergedEditedOnTimestamp,
                editedOn = System.currentTimeMillis()
            )
            transactionRepository.updateTransaction(updatedTransaction, originalAmount)
//            customerRepository.updateCustomerBalance(updatedTransaction.customerId, amount - customerTransaction.amount, isCredit = customerTransaction.isCredit)

        }
        navController.popBackStack() // Go back to transaction list
    }
}