package com.arjun.len_denkhata.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.len_denkhata.R
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookExpenseCategory
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionEntity
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionType
import com.arjun.len_denkhata.data.repository.MonthBookRepository
import com.arjun.len_denkhata.data.utils.KeyboardEventHandler
import com.arjun.len_denkhata.di.MonthBookPreferences
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.YearMonth
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalDate
import javax.inject.Inject

data class UnsavedTransactionData(
    val amount: String = "",
    val calculatedResult: String = "",
    val description: String = "",
    val selectedExpenseCategory: MonthBookExpenseCategory = MonthBookExpenseCategory.GENERAL,
    val selectedDateMillis: Long = System.currentTimeMillis()
)

@HiltViewModel
class MonthBookViewModel @Inject constructor(
    private val repository: MonthBookRepository,
    private val context: Context,
    @MonthBookPreferences private val sharedPreferences: SharedPreferences
) : ViewModel() {

    companion object {
        private const val UNSAVED_TRANSACTION_KEY = "unsaved_transaction_"
        private const val GSON_KEY = "gson_data"
    }

    private val keyboardHandler = KeyboardEventHandler()
    private val gson = Gson()

    private val _transactions = MutableStateFlow<List<MonthBookTransactionEntity>>(emptyList())
    val transactions: StateFlow<List<MonthBookTransactionEntity>> = _transactions.asStateFlow()

    private val _loadingCalculations = MutableStateFlow(false)
    val loadingCalculations: StateFlow<Boolean> = _loadingCalculations.asStateFlow()

    private val _calculatedIncome = MutableStateFlow(0.0)
    val calculatedIncome: StateFlow<Double> = _calculatedIncome.asStateFlow()

    private val _calculatedExpense = MutableStateFlow(0.0)
    val calculatedExpense: StateFlow<Double> = _calculatedExpense.asStateFlow()

    private val _calculatedNetIncome = MutableStateFlow(0.0)
    val calculatedNetIncome: StateFlow<Double> = _calculatedNetIncome.asStateFlow()

    private val _calculatedAvgIncome = MutableStateFlow(0.0)
    val calculatedAvgIncome: StateFlow<Double> = _calculatedAvgIncome.asStateFlow()

    private val _calculatedAvgIncomeOnIncomeDays = MutableStateFlow(0.0)
    val calculatedAvgIncomeOnIncomeDays: StateFlow<Double> = _calculatedAvgIncomeOnIncomeDays.asStateFlow()

    private val _calculatedExpenseByCategory = MutableStateFlow(emptyMap<MonthBookExpenseCategory, Double>())
    val calculatedExpenseByCategory: StateFlow<Map<MonthBookExpenseCategory, Double>> = _calculatedExpenseByCategory.asStateFlow()

    private val _calculatedAvgExpenseByCategory = MutableStateFlow(emptyMap<MonthBookExpenseCategory, Double>())
    val calculatedAvgExpenseByCategory: StateFlow<Map<MonthBookExpenseCategory, Double>> = _calculatedAvgExpenseByCategory.asStateFlow()

    private val _monthlyTotals = MutableStateFlow<List<Pair<YearMonth, Pair<Double, Double>>>>(emptyList())
    val monthlyTotals: StateFlow<List<Pair<YearMonth, Pair<Double, Double>>>> = _monthlyTotals.asStateFlow()

    // State for transaction entry
    private val _amountTextFieldValue = MutableStateFlow("")
    val amountTextFieldValue: StateFlow<String> = _amountTextFieldValue.asStateFlow()

    private val _calculatedResult = MutableStateFlow("")
    val calculatedResult: StateFlow<String> = _calculatedResult.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _selectedExpenseCategory = MutableStateFlow(MonthBookExpenseCategory.GENERAL)
    val selectedExpenseCategory: StateFlow<MonthBookExpenseCategory> = _selectedExpenseCategory.asStateFlow()

    private val _transactionForEdit = MutableStateFlow<MonthBookTransactionEntity?>(null)
    val transactionForEdit: StateFlow<MonthBookTransactionEntity?> = _transactionForEdit.asStateFlow()

    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    private val _dateError = MutableStateFlow<String?>(null)
    val dateError: StateFlow<String?> = _dateError.asStateFlow()

    // Missing StateFlow variables that were referenced in the code
    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()

    private val _lastUnsavedTransactionType = MutableStateFlow<MonthBookTransactionType?>(null)
    val lastUnsavedTransactionType: StateFlow<MonthBookTransactionType?> = _lastUnsavedTransactionType.asStateFlow()

    private val calculationPrefix = context.getString(R.string.calculated_amount)

    init {
        viewModelScope.launch {
            repository.allTransactions.collectLatest {
                _transactions.value = it
            }
        }
    }

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

    fun updateExpenseCategory(category: MonthBookExpenseCategory) {
        _selectedExpenseCategory.value = category
    }

    fun updateDate(newDate: Date) {
        viewModelScope.launch(Dispatchers.Main) {
            if (newDate.after(Date())) {
                _dateError.value = context.getString(R.string.date_error_while_choosing)
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

    fun addTransaction(
        amount: Double,
        description: String,
        date: Date,
        type: MonthBookTransactionType,
        monthBookExpenseCategory: MonthBookExpenseCategory? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val finalAmount = keyboardHandler.getFinalAmount(_amountTextFieldValue.value, _calculatedResult.value)
            // Warn about potential precision loss
            val bigDecimalAmount = BigDecimal(finalAmount.toString())
            if (bigDecimalAmount.toDouble() != finalAmount) {
                Log.w("MonthBookViewModel", "Precision loss detected when converting $finalAmount to Double")
            }

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

            val newTransaction = MonthBookTransactionEntity(
                amount = finalAmount,
                description = description,
                type = type,
                timestamp = mergedTimestamp,
                expenseCategory = if (type == MonthBookTransactionType.EXPENSE) monthBookExpenseCategory else null
            )

            repository.insertTransaction(newTransaction)
            clearFromSharedPreferences(type)
            withContext(Dispatchers.Main) {
                _amountTextFieldValue.value = ""
                _calculatedResult.value = ""
                _description.value = ""
                _selectedExpenseCategory.value = MonthBookExpenseCategory.GENERAL
                _selectedDate.value = Date()
                _dateError.value = null
                _hasUnsavedChanges.value = false
                _lastUnsavedTransactionType.value = null
            }
        }
    }

    fun updateTransaction(
        existingTransaction: MonthBookTransactionEntity,
        amount: Double,
        description: String,
        date: Date,
        type: MonthBookTransactionType,
        monthBookExpenseCategory: MonthBookExpenseCategory? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val finalAmount = keyboardHandler.getFinalAmount(_amountTextFieldValue.value, _calculatedResult.value)
            // Warn about potential precision loss
            val bigDecimalAmount = BigDecimal(finalAmount.toString())
            if (bigDecimalAmount.toDouble() != finalAmount) {
                Log.w("MonthBookViewModel", "Precision loss detected when converting $finalAmount to Double")
            }

            val originalTimestampCalendar = Calendar.getInstance()
            originalTimestampCalendar.timeInMillis = existingTransaction.timestamp

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

            val updatedTransaction = existingTransaction.copy(
                amount = finalAmount,
                description = description,
                type = type,
                expenseCategory = monthBookExpenseCategory,
                edited = true,
                editedOn = System.currentTimeMillis(),
                timestamp = mergedTimestamp
            )
            repository.updateTransaction(updatedTransaction)
            clearFromSharedPreferences(type)

            withContext(Dispatchers.Main) {
                _amountTextFieldValue.value = ""
                _calculatedResult.value = ""
                _description.value = ""
                _selectedExpenseCategory.value = MonthBookExpenseCategory.GENERAL
                _selectedDate.value = Date()
                _dateError.value = null
                _hasUnsavedChanges.value = false
                _lastUnsavedTransactionType.value = null
            }
        }
    }

    fun deleteTransaction(transaction: MonthBookTransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transaction)
        }
    }

    fun loadTransactionById(transactionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = repository.getTransactionById(transactionId).firstOrNull()
            withContext(Dispatchers.Main) {
                _transactionForEdit.value = transaction
                transaction?.let {
                    _amountTextFieldValue.value = it.amount.toString()
                    _description.value = it.description
                    _selectedExpenseCategory.value = it.expenseCategory ?: MonthBookExpenseCategory.GENERAL
                    // Extract the Date part from the transaction's timestamp
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = it.timestamp
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

    suspend fun getTransactionByIdOnce(transactionId: Long): MonthBookTransactionEntity? {
        return repository.getTransactionById(transactionId).firstOrNull()
    }

    suspend fun calculateTotalIncome(): Double {
        return _transactions.value.filter { it.type == MonthBookTransactionType.INCOME }.sumOf { it.amount }
    }

    suspend fun calculateTotalExpense(): Double {
        return _transactions.value.filter { it.type == MonthBookTransactionType.EXPENSE }.sumOf { it.amount }
    }

    suspend fun getExpensesByCategory(): Map<MonthBookExpenseCategory, Double> {
        return _transactions.value
            .filter { it.type == MonthBookTransactionType.EXPENSE && it.expenseCategory != null }
            .groupBy { it.expenseCategory!! }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    suspend fun calculateMonthData() {
        _loadingCalculations.value = true
        delay(1000)
        val currentMonth = YearMonth.now()
        val calendar = Calendar.getInstance()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val currentMonthTransactions = _transactions.value.filter { transaction ->
            calendar.timeInMillis = transaction.timestamp
            val transactionYearMonth = YearMonth.of(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1
            )
            transactionYearMonth == currentMonth
        }

        val incomeTransactions = currentMonthTransactions.filter { it.type == MonthBookTransactionType.INCOME }
        val totalCurrentMonthIncome = incomeTransactions.sumOf { it.amount }
        val totalCurrentMonthExpense = currentMonthTransactions.filter { it.type == MonthBookTransactionType.EXPENSE }.sumOf { it.amount }

        _calculatedIncome.value = totalCurrentMonthIncome
        _calculatedExpense.value = totalCurrentMonthExpense
        _calculatedNetIncome.value = _calculatedIncome.value - _calculatedExpense.value

        val daysWithIncome = incomeTransactions.distinctBy { transaction ->
            calendar.timeInMillis = transaction.timestamp
            dateFormatter.format(calendar.time)
        }.count()

        _calculatedAvgIncomeOnIncomeDays.value = if (daysWithIncome > 0 && totalCurrentMonthIncome > 0) {
            totalCurrentMonthIncome / daysWithIncome.toDouble()
        } else {
            0.0
        }

        val daysInMonthUpToNow = if (YearMonth.now() == currentMonth) {
            LocalDate.now().dayOfMonth
        } else {
            currentMonth.lengthOfMonth()
        }

        _calculatedAvgIncome.value = if (daysInMonthUpToNow > 0 && totalCurrentMonthIncome > 0) {
            totalCurrentMonthIncome / daysInMonthUpToNow.toDouble()
        } else {
            0.0
        }

        _calculatedExpenseByCategory.value = currentMonthTransactions
            .filter { it.type == MonthBookTransactionType.EXPENSE && it.expenseCategory != null }
            .groupBy { it.expenseCategory!! }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        _calculatedAvgExpenseByCategory.value = _calculatedExpenseByCategory.value.mapValues { (category, total) ->
            val countInCategory = currentMonthTransactions.count {
                it.type == MonthBookTransactionType.EXPENSE && it.expenseCategory == category
            }
            if (countInCategory > 0) {
                total / countInCategory.toDouble()
            } else {
                0.0
            }
        }

        _monthlyTotals.value = _transactions.value.groupBy { transaction ->
            calendar.timeInMillis = transaction.timestamp
            YearMonth.of(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1
            )
        }.mapValues { (_, monthlyTransactions) ->
            val income = monthlyTransactions.filter { it.type == MonthBookTransactionType.INCOME }
                .sumOf { it.amount }
            val expense = monthlyTransactions.filter { it.type == MonthBookTransactionType.EXPENSE }
                .sumOf { it.amount }
            Pair(income, expense)
        }.toList()
            .sortedByDescending { it.first }

        _loadingCalculations.value = false
    }

    private fun saveToSharedPreferences(transactionType: MonthBookTransactionType) {
        val key = "${UNSAVED_TRANSACTION_KEY}${transactionType.name}"
        val unsavedData = UnsavedTransactionData(
            amount = _amountTextFieldValue.value,
            calculatedResult = _calculatedResult.value,
            description = _description.value,
            selectedExpenseCategory = _selectedExpenseCategory.value,
            selectedDateMillis = _selectedDate.value.time
        )

        val jsonString = gson.toJson(unsavedData)
        sharedPreferences.edit().apply {
            putString(key, jsonString)
            apply()
        }
    }

    private fun loadFromSharedPreferences(transactionType: MonthBookTransactionType): UnsavedTransactionData? {
        val key = "${UNSAVED_TRANSACTION_KEY}${transactionType.name}"
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

    private fun clearFromSharedPreferences(transactionType: MonthBookTransactionType) {
        val key = "${UNSAVED_TRANSACTION_KEY}${transactionType.name}"
        sharedPreferences.edit().apply {
            remove(key)
            apply()
        }
    }

    private fun hasUnsavedData(): Boolean {
        return _amountTextFieldValue.value.isNotEmpty() || _description.value.isNotEmpty()
    }

    fun saveUnsavedTransaction(transactionType: MonthBookTransactionType) {
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

    fun loadUnsavedTransaction(transactionType: MonthBookTransactionType) {
        viewModelScope.launch(Dispatchers.IO) {
            val unsavedData = loadFromSharedPreferences(transactionType)
            if (unsavedData != null) {
                withContext(Dispatchers.Main) {
                    _amountTextFieldValue.value = unsavedData.amount
                    _calculatedResult.value = unsavedData.calculatedResult
                    _description.value = unsavedData.description
                    _selectedExpenseCategory.value = unsavedData.selectedExpenseCategory
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
            // Clear from SharedPreferences for both transaction types
            clearFromSharedPreferences(MonthBookTransactionType.INCOME)
            clearFromSharedPreferences(MonthBookTransactionType.EXPENSE)

            withContext(Dispatchers.Main) {
                _hasUnsavedChanges.value = false
                _lastUnsavedTransactionType.value = null
                _amountTextFieldValue.value = ""
                _calculatedResult.value = ""
                _description.value = ""
                _selectedExpenseCategory.value = MonthBookExpenseCategory.GENERAL
                _selectedDate.value = Date()
                _dateError.value = null
                keyboardHandler.clearCache()
            }
        }
    }

    fun clearUnsavedTransactionForType(transactionType: MonthBookTransactionType) {
        viewModelScope.launch(Dispatchers.IO) {
            clearFromSharedPreferences(transactionType)
            withContext(Dispatchers.Main) {
                _hasUnsavedChanges.value = false
                _lastUnsavedTransactionType.value = null
                _amountTextFieldValue.value = ""
                _calculatedResult.value = ""
                _description.value = ""
                _selectedExpenseCategory.value = MonthBookExpenseCategory.GENERAL
                _selectedDate.value = Date()
                _dateError.value = null
                keyboardHandler.clearCache()
            }
        }
    }
}