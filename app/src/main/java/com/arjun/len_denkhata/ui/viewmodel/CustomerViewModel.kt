package com.arjun.len_denkhata.ui.viewmodel

import android.content.ContentResolver
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.Screen
import com.arjun.len_denkhata.data.database.customer.CustomerEntity
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.arjun.len_denkhata.data.repository.customer.CustomerRepository
import com.arjun.len_denkhata.data.repository.customer.CustomerTransactionRepository
import com.arjun.len_denkhata.data.repository.LoginRepository
import com.arjun.len_denkhata.data.utils.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val customerTransactionRepository: CustomerTransactionRepository,
    private val loginRepository: LoginRepository
) : ViewModel() {

    private val _customers = MutableStateFlow<List<CustomerEntity>>(emptyList())
    val customers: StateFlow<List<CustomerEntity>> = _customers

    val transactions: StateFlow<List<CustomerTransactionEntity>> = customerTransactionRepository.allTransactions

    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomer

    private val _customerOverallBalance = MutableStateFlow(0.0)
    val customerOverallBalance: StateFlow<Double> = _customerOverallBalance

    private val _totalHaveToGive = MutableStateFlow(0.0)
    val totalHaveToGive: StateFlow<Double> = _totalHaveToGive

    private val _totalWillGet = MutableStateFlow(0.0)
    val totalWillGet: StateFlow<Double> = _totalWillGet

    val todayDue: StateFlow<Double> = customerTransactionRepository.todayDue

    private val _phoneNumberToMerge = MutableStateFlow("")
    val phoneNumberToMerge: StateFlow<String> = _phoneNumberToMerge

    private val _showCountryCodeDialog = MutableStateFlow(false)
    val showCountryCodeDialog: StateFlow<Boolean> = _showCountryCodeDialog

    private val _contactName = MutableStateFlow("")
    val contactName: StateFlow<String> = _contactName

    private val _transactionsByCustomer = MutableStateFlow<List<CustomerTransactionEntity>>(emptyList())
    val transactionsByCustomer: StateFlow<List<CustomerTransactionEntity>> = _transactionsByCustomer

    init {
        Log.d("testTag", "viewModel Initialized")
        viewModelScope.launch {
            loadCustomers()
//            loadAllTransactionsAndCalculateTotals()
        }
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            customerRepository.getAllCustomers().collectLatest { customers ->
                _customers.value = customers
                _totalWillGet.value = customerRepository.calculateWillGet(_customers.value)
                _totalHaveToGive.value = customerRepository.calculateHaveToGive(_customers.value)
            }
        }
    }

    private suspend fun loadAllTransactionsAndCalculateTotals() {
        val ownerId = UserSession.phoneNumber // Handle null ownerId
        customerTransactionRepository.fetchAllTransactions(ownerId!!)
        calculateTotals() // Calculate totals after loading transactions
    }
//    private fun calculateTodayDue(customers: List<CustomerEntity>) {
//        viewModelScope.launch {
//            _todayDue.value = customerTransactionRepository.calculateTodayDue()
//        }
//    }

    private suspend fun calculateTotals() {
//        viewModelScope.launch {_totalCredit.value = customerTransactionRepository.calculateHaveToGive()}
//        viewModelScope.launch {_totalDebit.value = customerTransactionRepository.calculateWillGet()}
//        viewModelScope.launch {_todayDue.value = customerTransactionRepository.calculateTodayDue()}
    }



    fun mergeCountryCodeAndAddCustomer(countryCode: String, contentResolver: ContentResolver, navController: NavHostController) {
        viewModelScope.launch {
            val fullNumber = countryCode + phoneNumberToMerge.value
            val name = _contactName.value // Use stored contact name
            val ownerId = loginRepository.mobileNumber ?: return@launch
            val customer = CustomerEntity(id = fullNumber, name = name, phone = fullNumber)
            customerRepository.insertCustomer(customer)
            viewModelScope.launch { loadCustomerBalance(customerId = fullNumber) }
            navController.navigate(Screen.CustomerTransaction.createRoute(customer.id))
            _showCountryCodeDialog.value = false
        }
    }

    fun dismissCountryCodeDialog() {
        _showCountryCodeDialog.value = false
    }

    fun validateAndProcessContact(name: String, number: String, navController: NavHostController, showToast: (String) -> Unit) {
        viewModelScope.launch {
            val cleanedNumber = number.replace(Regex("[^\\d+]"), "")
            if (cleanedNumber.length < 10) {
                showToast("Invalid number. Please enter a valid number.")
                return@launch
            }

            val lastTenDigits = cleanedNumber.takeLast(10)
            val countryCode = cleanedNumber.removeSuffix(lastTenDigits)

            if (countryCode.isNotEmpty()) {
                // Country code exists, add customer
                addCustomer(name, cleanedNumber, navController)
            } else {
                // Show country code dialog
                _contactName.value = name;
                _phoneNumberToMerge.value = lastTenDigits
                _showCountryCodeDialog.value = true
            }
        }
    }

    private fun addCustomer(name: String, number: String, navController: NavHostController) {
        viewModelScope.launch {
            // Insert the customer and get the generated ID.
            val customerId = number
            viewModelScope.launch {
                customerRepository.insertCustomer(CustomerEntity(id = number, name = name, phone = number))
            }
            viewModelScope.launch { loadCustomerBalance(customerId = number) }
            // Navigate after the insertion is complete.
            navController.navigate(Screen.CustomerTransaction.createRoute(customerId))
        }
    }

    fun loadTransactions(customerId: String) {
        viewModelScope.launch {
            customerTransactionRepository.getCustomerTransactionsByCustomerId(customerId).collectLatest {
                _transactionsByCustomer.value = it
            }
        }
    }

    fun deleteTransaction(transaction: CustomerTransactionEntity, deletedAmount: Double) {
        viewModelScope.launch {
            customerTransactionRepository.deleteTransaction(transaction)
            customerRepository.updateCustomerBalance(transaction.customerId, transaction.amount, isCredit = !transaction.isCredit)
        }
    }

    fun loadCustomerBalance(customerId: String) {
        viewModelScope.launch {
            customerRepository.getCustomerById(customerId).collectLatest { customer ->
                _selectedCustomer.value = customer
                Log.d("testTag", customer.toString())
                _customerOverallBalance.value = customer?.overallBalance ?: 0.0
            }
        }
    }

    fun deleteCustomer(customerEntity: CustomerEntity) {
        viewModelScope.launch {
            customerRepository.deleteCustomer(customerEntity)
        }
    }

    fun updateCustomer(updatedCustomer: CustomerEntity) {
        viewModelScope.launch {
            customerRepository.updateCustomer(updatedCustomer)
        }
    }

}