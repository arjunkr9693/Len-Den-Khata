package com.arjun.len_denkhata.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.arjun.len_denkhata.data.repository.customer.CustomerRepository
import com.arjun.len_denkhata.data.repository.customer.CustomerTransactionRepository
import com.arjun.len_denkhata.data.utils.KeyboardEventHandler
import com.arjun.len_denkhata.data.utils.UserSession
import com.arjun.len_denkhata.di.CustomerPreferences
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CustomerTransactionEntryViewModel @Inject constructor(
    private val transactionRepository: CustomerTransactionRepository,
    private val customerRepository: CustomerRepository,
    private val context: Context,
    @CustomerPreferences private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val keyboardHandler = KeyboardEventHandler()
    private val gson = Gson()

    companion object {
        private const val UNSAVED_TRANSACTION_KEY = "unsaved_customer_transaction_"
    }

    // State exposed to the UI
    private val _amountTextFieldValue = MutableStateFlow("")
    val amountTextFieldValue = _amountTextFieldValue.asStateFlow()

    private val _calculatedResult = MutableStateFlow("")
    val calculatedResult = _calculatedResult.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _transactionForEdit = MutableStateFlow<CustomerTransactionEntity?>(null)
    val transactionForEdit = _transactionForEdit.asStateFlow()

    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate = _selectedDate.asStateFlow()

    private val _dateError = MutableStateFlow<String?>(null)
    val dateError = _dateError.asStateFlow()

    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges = _hasUnsavedChanges.asStateFlow()

    private val _lastUnsavedTransactionType = MutableStateFlow<String?>(null)
    val lastUnsavedTransactionType = _lastUnsavedTransactionType.asStateFlow()

    private val calculationPrefix = context.getString(com.arjun.len_denkhata.R.string.calculated_amount)

    // Handle input events
    fun handleDigitInput(digit: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val currentValue = _amountTextFieldValue.value
            val newValue = keyboardHandler.handleDigitInput(currentValue, digit)
            _amountTextFieldValue.value = newValue
            updateCalculation(newValue)
        }
    }

    fun handleOperatorInput(operator: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val currentValue = _amountTextFieldValue.value
            val newValue = keyboardHandler.handleOperatorInput(currentValue, operator)
            _amountTextFieldValue.value = newValue
            updateCalculation(newValue)
        }
    }

    fun handleDecimalInput() {
        viewModelScope.launch(Dispatchers.Default) {
            val currentValue = _amountTextFieldValue.value
            val newValue = keyboardHandler.handleDecimalInput(currentValue)
            _amountTextFieldValue.value = newValue
            updateCalculation(newValue)
        }
    }

    fun handlePercentageInput() {
        viewModelScope.launch(Dispatchers.Default) {
            val currentValue = _amountTextFieldValue.value
            val newValue = keyboardHandler.handlePercentageInput(currentValue)
            _amountTextFieldValue.value = newValue
            updateCalculation(newValue)
        }
    }

    fun handleBackspace() {
        viewModelScope.launch(Dispatchers.Default) {
            val currentValue = _amountTextFieldValue.value
            val newValue = keyboardHandler.handleBackspace(currentValue)
            _amountTextFieldValue.value = newValue
            updateCalculation(newValue)
        }
    }

    fun clearInput() {
        viewModelScope.launch(Dispatchers.Default) {
            keyboardHandler.clearCache()
            _amountTextFieldValue.value = ""
            _calculatedResult.value = ""
        }
    }

    fun updateDescription(newDescription: String) {
        _description.value = newDescription
    }

    fun updateDate(newDate: Date) {
        viewModelScope.launch(Dispatchers.Main) {
            if (newDate.after(Date())) {
                _dateError.value = context.getString(com.arjun.len_denkhata.R.string.date_error_while_choosing)
            } else {
                _dateError.value = null
                _selectedDate.value = newDate
            }
        }
    }

    private suspend fun updateCalculation(newValue: String) {
        val result = keyboardHandler.calculateWithCaching(newValue, calculationPrefix)
        withContext(Dispatchers.Main) {
            _calculatedResult.value = result
        }
    }

    fun saveTransaction(
        customerId: String,
        isCredit: Boolean,
        navController: NavHostController
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val finalAmount = keyboardHandler.getFinalAmount(_amountTextFieldValue.value, _calculatedResult.value)
            val bigDecimalAmount = BigDecimal(finalAmount.toString())
            if (bigDecimalAmount.toDouble() != finalAmount) {
                Log.w("CustomerTransactionEntryViewModel", "Precision loss detected when converting $finalAmount to Double")
            }
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()

            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)
            val millisecond = calendar.get(Calendar.MILLISECOND)

            val dateCalendar = Calendar.getInstance()
            dateCalendar.time = _selectedDate.value

            dateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            dateCalendar.set(Calendar.MINUTE, minute)
            dateCalendar.set(Calendar.SECOND, second)
            dateCalendar.set(Calendar.MILLISECOND, millisecond)

            val mergedTimestamp = dateCalendar.timeInMillis

            val transaction = CustomerTransactionEntity(
                ownerId = UserSession.phoneNumber!!,
                customerId = customerId,
                amount = finalAmount,
                date = _selectedDate.value,
                description = _description.value,
                isCredit = isCredit,
                timestamp = mergedTimestamp
            )

            Log.d("DateCheck", "Date: ${_selectedDate.value}")
            transactionRepository.insertCustomerTransaction(transaction)

            // Update customer's last updated timestamp
            updateCustomerLastUpdated(customerId)

            // Clear unsaved data
            clearFromSharedPreferences(if (isCredit) "You Got" else "You Gave")

            withContext(Dispatchers.Main) {
                _amountTextFieldValue.value = ""
                _calculatedResult.value = ""
                _description.value = ""
                _selectedDate.value = Date()
                _dateError.value = null
                _hasUnsavedChanges.value = false
                _lastUnsavedTransactionType.value = null
                navController.popBackStack()
            }
        }
    }

    fun updateTransaction(
        originalAmount: Double,
        customerTransaction: CustomerTransactionEntity,
        navController: NavHostController
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val finalAmount = keyboardHandler.getFinalAmount(_amountTextFieldValue.value, _calculatedResult.value)
            val bigDecimalAmount = BigDecimal(finalAmount.toString())
            if (bigDecimalAmount.toDouble() != finalAmount) {
                Log.w("CustomerTransactionEntryViewModel", "Precision loss detected when converting $finalAmount to Double")
            }
            val originalTimestampCalendar = Calendar.getInstance()
            originalTimestampCalendar.timeInMillis = customerTransaction.timestamp

            val hourOfDay = originalTimestampCalendar.get(Calendar.HOUR_OF_DAY)
            val minute = originalTimestampCalendar.get(Calendar.MINUTE)
            val second = originalTimestampCalendar.get(Calendar.SECOND)
            val millisecond = originalTimestampCalendar.get(Calendar.MILLISECOND)

            val newDateCalendar = Calendar.getInstance()
            newDateCalendar.time = _selectedDate.value

            newDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            newDateCalendar.set(Calendar.MINUTE, minute)
            newDateCalendar.set(Calendar.SECOND, second)
            newDateCalendar.set(Calendar.MILLISECOND, millisecond)

            val mergedTimestamp = newDateCalendar.timeInMillis

            val updatedTransaction = customerTransaction.copy(
                amount = finalAmount,
                description = _description.value,
                isEdited = true,
                date = _selectedDate.value,
                timestamp = mergedTimestamp,
                editedOn = System.currentTimeMillis()
            )
            transactionRepository.updateTransaction(updatedTransaction, originalAmount)

            // Update customer's last updated timestamp
            updateCustomerLastUpdated(customerTransaction.customerId)

            // Clear unsaved data
            clearFromSharedPreferences(if (customerTransaction.isCredit) "You Got" else "You Gave")

            withContext(Dispatchers.Main) {
                _amountTextFieldValue.value = ""
                _calculatedResult.value = ""
                _description.value = ""
                _selectedDate.value = Date()
                _dateError.value = null
                _hasUnsavedChanges.value = false
                _lastUnsavedTransactionType.value = null
                navController.popBackStack()
            }
        }
    }

    private suspend fun updateCustomerLastUpdated(customerId: String) {
        val customer = customerRepository.getCustomerById(customerId).first()
        customer?.let {
            val updatedCustomer = it.copy(lastUpdated = System.currentTimeMillis())
            customerRepository.updateCustomer(updatedCustomer)
        }
    }

    fun loadTransactionByTransactionId(transactionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = transactionRepository.getTransactionByTransactionId(transactionId)
            withContext(Dispatchers.Main) {
                _transactionForEdit.value = transaction
                transaction?.let {
                    _amountTextFieldValue.value = it.amount.toString()
                    _description.value = it.description ?: ""
                    // Extract the Date part from the transaction's timestamp
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = it.date.time
                    }
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                    // Create a new Date object with only the date components
                    val extractedDate = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time
                    _selectedDate.value = extractedDate
                    keyboardHandler.clearCache()
                }
            }
        }
    }

    private fun saveToSharedPreferences(transactionType: String) {
        val key = "${UNSAVED_TRANSACTION_KEY}${transactionType}"
        val unsavedData = UnsavedTransactionData(
            amount = _amountTextFieldValue.value,
            calculatedResult = _calculatedResult.value,
            description = _description.value,
            selectedDateMillis = _selectedDate.value.time
        )

        val jsonString = gson.toJson(unsavedData)
        sharedPreferences.edit().apply {
            putString(key, jsonString)
            apply()
        }
    }

    private fun loadFromSharedPreferences(transactionType: String): UnsavedTransactionData? {
        val key = "${UNSAVED_TRANSACTION_KEY}${transactionType}"
        val jsonString = sharedPreferences.getString(key, null)

        return if (jsonString != null) {
            try {
                gson.fromJson(jsonString, UnsavedTransactionData::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    private fun clearFromSharedPreferences(transactionType: String) {
        val key = "${UNSAVED_TRANSACTION_KEY}${transactionType}"
        sharedPreferences.edit().apply {
            remove(key)
            apply()
        }
    }

    private fun hasUnsavedData(): Boolean {
        return _amountTextFieldValue.value.isNotEmpty() || _description.value.isNotEmpty()
    }

    fun saveUnsavedTransaction(transactionType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (hasUnsavedData()) {
                saveToSharedPreferences(transactionType)
                withContext(Dispatchers.Main) {
                    _hasUnsavedChanges.value = true
                    _lastUnsavedTransactionType.value = transactionType
                }
            }
        }
    }

    fun loadUnsavedTransaction(transactionType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val unsavedData = loadFromSharedPreferences(transactionType)
            if (unsavedData != null) {
                withContext(Dispatchers.Main) {
                    _amountTextFieldValue.value = unsavedData.amount
                    _calculatedResult.value = unsavedData.calculatedResult
                    _description.value = unsavedData.description
                    _selectedDate.value = Date(unsavedData.selectedDateMillis)
                    _hasUnsavedChanges.value = true
                    _lastUnsavedTransactionType.value = transactionType

                    // Update calculation if amount exists
                    if (unsavedData.amount.isNotEmpty()) {
                        updateCalculation(unsavedData.amount)
                    }
                }
            }
        }
    }

    fun clearUnsavedTransaction() {
        viewModelScope.launch(Dispatchers.IO) {
            clearFromSharedPreferences("You Got")
            clearFromSharedPreferences("textfield")
            withContext(Dispatchers.Main) {
                _hasUnsavedChanges.value = false
                _lastUnsavedTransactionType.value = null
                _amountTextFieldValue.value = ""
                _calculatedResult.value = ""
                _description.value = ""
                _selectedDate.value = Date()
                _dateError.value = null
                keyboardHandler.clearCache()
            }
        }
    }

    fun clearUnsavedTransactionForType(transactionType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            clearFromSharedPreferences(transactionType)
            withContext(Dispatchers.Main) {
                _hasUnsavedChanges.value = false
                _lastUnsavedTransactionType.value = null
                _amountTextFieldValue.value = ""
                _calculatedResult.value = ""
                _description.value = ""
                _selectedDate.value = Date()
                _dateError.value = null
                keyboardHandler.clearCache()
            }
        }
    }
}