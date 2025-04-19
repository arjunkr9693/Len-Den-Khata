package com.arjun.len_denkhata.monthbook.ui.viewmodel

import androidx.lifecycle.*
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookExpenseCategory
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionEntity
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionType
import com.arjun.len_denkhata.data.repository.MonthBookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MonthBookViewModel @Inject constructor(private val repository: MonthBookRepository) : ViewModel() {
    private val _transactions = MutableStateFlow<List<MonthBookTransactionEntity>>(emptyList())
    val transactions: StateFlow<List<MonthBookTransactionEntity>> = _transactions

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

    private val _calculatedAvgIncomeOnIncomeDays = MutableStateFlow(0.0) // New StateFlow with a more relevant name
    val calculatedAvgIncomeOnIncomeDays: StateFlow<Double> = _calculatedAvgIncomeOnIncomeDays.asStateFlow()


    private val _calculatedExpenseByCategory = MutableStateFlow(emptyMap<MonthBookExpenseCategory, Double>())
    val calculatedExpenseByCategory: StateFlow<Map<MonthBookExpenseCategory, Double>> = _calculatedExpenseByCategory.asStateFlow()

    private val _calculatedAvgExpenseByCategory = MutableStateFlow(emptyMap<MonthBookExpenseCategory, Double>())
    val calculatedAvgExpenseByCategory: StateFlow<Map<MonthBookExpenseCategory, Double>> = _calculatedAvgExpenseByCategory.asStateFlow()

    private val _monthlyTotals = MutableStateFlow<List<Pair<YearMonth, Pair<Double, Double>>>>(emptyList())
    val monthlyTotals: StateFlow<List<Pair<YearMonth, Pair<Double, Double>>>> = _monthlyTotals.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allTransactions.collectLatest {
                _transactions.value = it
            }
        }
    }

    fun addTransaction(
        amount: Double,
        description: String,
        date: Date,
        type: MonthBookTransactionType, // Using MonthBookTransactionType
        monthBookExpenseCategory: MonthBookExpenseCategory? = null // Using MonthBookExpenseCategory
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

            val newTransaction = MonthBookTransactionEntity(
                amount = amount,
                description = description,
                type = type,
                expenseCategory = monthBookExpenseCategory,
                timestamp = mergedTimestamp
            )
            repository.insertTransaction(newTransaction)
        }
    }

    fun updateTransaction(
        existingTransaction: MonthBookTransactionEntity,
        amount: Double,
        description: String,
        date: Date,
        type: MonthBookTransactionType, // Using MonthBookTransactionType
        monthBookExpenseCategory: MonthBookExpenseCategory? = null // Using MonthBookExpenseCategory
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

            val updatedTransaction = existingTransaction.copy(
                amount = amount,
                description = description,
                type = type,
                expenseCategory = monthBookExpenseCategory,
                edited = true,
                editedOn = System.currentTimeMillis(),
                timestamp = mergedTimestamp // Using the merged timestamp for the main timestamp as well
            )
            repository.updateTransaction(updatedTransaction) // Assuming OnConflictStrategy.REPLACE in DAO
        }
    }

    fun deleteTransaction(transaction: MonthBookTransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    suspend fun getTransactionByIdOnce(transactionId: Long): MonthBookTransactionEntity? {
        return repository.getTransactionById(transactionId).firstOrNull() // Collects the first value and then cancels
    }

    suspend fun calculateTotalIncome(): Double {
        return _transactions.value.filter { it.type == MonthBookTransactionType.INCOME }.sumOf { it.amount }
    }

    suspend fun calculateTotalExpense(): Double {
        return _transactions.value.filter { it.type == MonthBookTransactionType.EXPENSE }.sumOf { it.amount }
    }

    suspend fun getExpensesByCategory(): Map<MonthBookExpenseCategory, Double> { // Using MonthBookExpenseCategory
        return _transactions.value
            .filter { it.type == MonthBookTransactionType.EXPENSE && it.expenseCategory != null }
            .groupBy { it.expenseCategory!! }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    fun calculateMonthData() {
        _loadingCalculations.value = true
        val currentMonth = java.time.YearMonth.now()
        val calendar = Calendar.getInstance()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val currentMonthTransactions = _transactions.value.filter { transaction ->
            calendar.timeInMillis = transaction.timestamp
            val transactionYearMonth = java.time.YearMonth.of(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1 // Calendar's month is 0-based
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

        // Calculate number of days from month start to current date
        val daysInMonthUpToNow = if (YearMonth.now() == currentMonth) {
            // If current month, use today's date
            LocalDate.now().dayOfMonth
        } else {
            // If past month, use total days in that month
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
            java.time.YearMonth.of(
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

}