package com.arjun.len_denkhata.ui.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val customerTransactionRepository: CustomerTransactionRepository,
    private val loginRepository: LoginRepository,
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {

    private val _customers = MutableStateFlow<List<CustomerEntity>>(emptyList())
    val customers: StateFlow<List<CustomerEntity>> = _customers

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

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

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


    fun mergeCountryCodeAndAddCustomer(countryCode: String, contentResolver: ContentResolver, navController: NavHostController) {
        viewModelScope.launch {
            if(countryCode.isEmpty()){
                Toast.makeText(navController.context, "Please enter country code", Toast.LENGTH_SHORT).show()
                return@launch
            }else if(countryCode[0] != '+') {
                Toast.makeText(navController.context, "Please enter valid country code", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val fullNumber = countryCode + phoneNumberToMerge.value
            val name = contactName.value.ifEmpty { fullNumber }
            addCustomer(name, fullNumber, navController)
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
    suspend fun updateContactNamesFromPhonebook() {
        uiScope.launch {
            val updatedCustomers = _customers.value.map { customer ->
                val contactName = withContext(Dispatchers.IO) {
                    getContactNameFromPhonebook(customer.phone.takeLast(10))
                }
                if (contactName != null && customer.name != contactName) {
                    customer.copy(name = contactName)
                } else {
                    customer
                }
            }
            _customers.value = updatedCustomers
            // Optionally, persist the updated contact names if needed
            updatedCustomers.forEach { customer ->
                customerRepository.updateCustomer(customer)
            }
        }
    }

    private suspend fun getContactNameFromPhonebook(phoneNumberLast10Digits: String): String? = withContext(Dispatchers.IO) {
        val contentResolver: ContentResolver = applicationContext.contentResolver
        val uri: Uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumberLast10Digits))
        var contactName: String? = null

        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        try {
            val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    contactName = it.getString(nameIndex)
                }
            }
        } catch (e: SecurityException) {
            // Handle the case where READ_CONTACTS permission is not granted
            android.util.Log.e("ContactLookup", "Permission to read contacts not granted: ${e.message}")
            // You might want to inform the user about the missing permission
        }
        return@withContext contactName
    }

}