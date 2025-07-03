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
import com.arjun.len_denkhata.data.database.TransactionUiModel
import com.arjun.len_denkhata.data.database.customer.CustomerEntity
import com.arjun.len_denkhata.data.repository.customer.CustomerRepository
import com.arjun.len_denkhata.data.repository.customer.CustomerTransactionRepository
import com.arjun.len_denkhata.data.utils.DateFormatters
import com.arjun.len_denkhata.data.utils.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val customerTransactionRepository: CustomerTransactionRepository,
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

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _groupedTransactions = MutableStateFlow<Map<String, List<TransactionUiModel>>>(emptyMap())
    val groupedTransactions: StateFlow<Map<String, List<TransactionUiModel>>> = _groupedTransactions

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filteredGroupedTransactions = MutableStateFlow<Map<String, List<TransactionUiModel>>>(emptyMap())
    val filteredGroupedTransactions: StateFlow<Map<String, List<TransactionUiModel>>> = _filteredGroupedTransactions

    init {
        Log.d("testTag", "viewModel Initialized")
        viewModelScope.launch {
            loadCustomers()
            calculateTodayDue()
            observeSearchQuery()
        }
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            customerRepository.getAllCustomers().collectLatest { customers ->
                _customers.value = customers.sortedByDescending { it.lastUpdated }
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
            // Get contact info including profile picture
            val contactInfo = withContext(Dispatchers.IO) {
                getContactInfoFromPhonebook(number.takeLast(10))
            }

            val customerId = number
            val customerEntity = CustomerEntity(
                id = number,
                name = name,
                phone = number,
                isNameModifiedByUser = true, // Since user is manually adding
                profilePictureUri = contactInfo?.profilePictureUri
            )

            viewModelScope.launch {
                customerRepository.insertCustomer(customerEntity)
            }
            viewModelScope.launch {
                setSelectedCustomerById(customerId)
            }
            // Navigate after the insertion is complete.
            navController.navigate(Screen.CustomerTransaction.createRoute(customerId))
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // Avoid recomputing too frequently
                .distinctUntilChanged()
                .collectLatest {
                    filterTransactions()
                }
        }
    }

    private fun filterTransactions() {
        val query = _searchQuery.value
        val currentTransactions = _groupedTransactions.value

        if (query.isEmpty()) {
            _filteredGroupedTransactions.value = currentTransactions
            return
        }

        val filtered = currentTransactions.mapValues { (_, transactions) ->
            transactions.filter {
                it.amount.toString().contains(query, ignoreCase = true) ||
                        it.description?.contains(query, ignoreCase = true) == true
            }
        }.filterValues { it.isNotEmpty() }

        _filteredGroupedTransactions.value = filtered
    }

    fun loadTransactions(customerId: String) {
        viewModelScope.launch {
            customerTransactionRepository.getCustomerTransactionsByCustomerId(customerId).collectLatest { transactions ->
                val uiTransactions = transactions.map { it.toUiModel() }

                val groupedAndSorted = uiTransactions.groupBy {
                    DateFormatters.dateGroupFormat.format(
                        DateFormatters.fullTimestampFormat.parse(it.formattedTimestamp) ?: Date()
                    )
                }.toSortedMap()

                _groupedTransactions.value = groupedAndSorted
                _filteredGroupedTransactions.value = groupedAndSorted // Initialize filtered with all transactions
            }
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            customerTransactionRepository.deleteTransaction(transactionId)
        }
    }

    fun deleteCustomer(customerEntity: CustomerEntity) {
        viewModelScope.launch {
            customerRepository.deleteCustomer(customerEntity)
        }
    }

    fun updateCustomer(updatedCustomer: CustomerEntity) {
        viewModelScope.launch {
            customerRepository.updateCustomer(updatedCustomer.copy(isNameModifiedByUser = true))
        }
    }

    // Update customer name manually (sets isNameModifiedByUser to true)
    fun updateCustomerName(customerId: String, newName: String) {
        viewModelScope.launch {
            val customer = customerRepository.getCustomerById(customerId).first()
            customer?.let {
                val updatedCustomer = it.copy(
                    name = newName,
                    isNameModifiedByUser = true,
                    lastUpdated = System.currentTimeMillis()
                )
                customerRepository.updateCustomer(updatedCustomer)
            }
        }
    }

    suspend fun updateContactDetailFromPhonebook() {
        uiScope.launch {
            val updatedCustomers = _customers.value.map { customer ->
                // Only update if name was not manually modified by user
                if (!customer.isNameModifiedByUser) {
                    val contactInfo = withContext(Dispatchers.IO) {
                        getContactInfoFromPhonebook(customer.phone.takeLast(10))
                    }
                    if (contactInfo?.name != null && customer.name != contactInfo.name) {
                        customer.copy(
                            name = contactInfo.name,
                            profilePictureUri = contactInfo.profilePictureUri,
                            lastUpdated = System.currentTimeMillis()
                        )
                    } else {
                        customer
                    }
                } else {
                    customer
                }
            }
            _customers.value = updatedCustomers
            // Persist the updated contact names
            updatedCustomers.forEach { customer ->
                customerRepository.updateCustomer(customer)
            }
        }
    }

    private data class ContactInfo(
        val name: String?,
        val profilePictureUri: String?
    )

    private suspend fun getContactInfoFromPhonebook(phoneNumberLast10Digits: String): ContactInfo? = withContext(Dispatchers.IO) {
        val contentResolver: ContentResolver = applicationContext.contentResolver
        val uri: Uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumberLast10Digits))
        var contactInfo: ContactInfo? = null

        val projection = arrayOf(
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_URI
        )

        try {
            val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    val photoIndex = it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_URI)

                    val contactName = it.getString(nameIndex)
                    val photoUri = it.getString(photoIndex)

                    contactInfo = ContactInfo(contactName, photoUri)
                }
            }
        } catch (e: SecurityException) {
            android.util.Log.e("ContactLookup", "Permission to read contacts not granted: ${e.message}")
        }
        return@withContext contactInfo
    }

    private fun calculateTodayDue() {
        viewModelScope.launch {
            customerTransactionRepository.calculateTodayDue()
        }
    }

    private var selectedCustomerJob: Job? = null

    fun setSelectedCustomerById(customerId: String) {
        selectedCustomerJob?.cancel()

        selectedCustomerJob = viewModelScope.launch {
            customerRepository.getCustomerById(customerId).collectLatest {
                _selectedCustomer.value = it
            }
        }
    }

    fun getLastUpdatedMessage(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 0 -> "Updated in the future"
            diff < 60000 -> {
                val seconds = (diff / 1000).toInt()
                "Updated $seconds ${if (seconds == 1) "second" else "seconds"} ago"
            }
            diff < 3600000 -> {
                val minutes = (diff / 60000).toInt()
                "Updated $minutes ${if (minutes == 1) "minute" else "minutes"} ago"
            }
            diff < 86400000 -> {
                val hours = (diff / 3600000).toInt()
                "Updated $hours ${if (hours == 1) "hour" else "hours"} ago"
            }
            diff < 604800000 -> {
                val days = (diff / 86400000).toInt()
                "Updated $days ${if (days == 1) "day" else "days"} ago"
            }
            diff < 2592000000 -> {
                val weeks = (diff / 604800000).toInt()
                "Updated $weeks ${if (weeks == 1) "week" else "weeks"} ago"
            }
            diff < 31536000000 -> {
                val months = (diff / 2592000000).toInt()
                "Updated $months ${if (months == 1) "month" else "months"} ago"
            }
            else -> {
                val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(java.util.Date(timestamp))
                "Updated on $date"
            }
        }
    }
}