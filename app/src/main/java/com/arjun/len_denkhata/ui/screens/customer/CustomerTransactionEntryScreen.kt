package com.arjun.len_denkhata.ui.screens.customer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.R
import com.arjun.len_denkhata.data.utils.KeyboardEventHandler
import com.arjun.len_denkhata.ui.components.CustomAmountTextField
import com.arjun.len_denkhata.ui.components.CustomTopBarWithIcon
import com.arjun.len_denkhata.ui.components.DatePickerModal
import com.arjun.len_denkhata.ui.screens.CustomNumericKeyboard
import com.arjun.len_denkhata.ui.viewmodel.CustomerTransactionEntryViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomerTransactionEntryScreen(
    navController: NavHostController,
    customerId: String?,
    transactionType: String,
    viewModel: CustomerTransactionEntryViewModel = hiltViewModel(),
    isEditing: Boolean = false,
    transactionId: Long = -1L
) {
    // Initialize keyboard handler - reuse same instance
    val keyboardHandler = remember { KeyboardEventHandler() }

    // State variables
    var amountTextFieldValue by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var calculatedResult by remember { mutableStateOf("") }
    var showCustomKeyboard by remember { mutableStateOf(false) }
    var amountFieldFocused by remember { mutableStateOf(false) }
    var descriptionFieldFocused by remember { mutableStateOf(false) }
    var clearAmountFieldFocus by remember { mutableStateOf(false) }

    // Date handling
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Date()) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var dateError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val customerTransactionEntity by viewModel.transactionForEdit.collectAsState()

    // Memoized calculation prefix
    val calculationPrefix = remember { context.getString(R.string.calculated_amount) }

    // Load existing transaction for editing
    LaunchedEffect(isEditing, transactionId) {
        if (isEditing && transactionId != -1L) {
            viewModel.loadTransactionByTransactionId(transactionId)
            customerTransactionEntity?.let { transaction ->
                amountTextFieldValue = transaction.amount.toString()
                description = transaction.description ?: ""

                // Extract the Date part from the transaction's timestamp
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = transaction.date.time
                }
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

                // Create a new Date object with only the date components
                val extractedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                selectedDate = extractedDate
                keyboardHandler.clearCache() // Clear cache when loading existing transaction
            }
        }
    }

    // Handle keyboard visibility
    LaunchedEffect(amountFieldFocused) {
        showCustomKeyboard = amountFieldFocused
    }

    // Handle focus and keyboard visibility
    LaunchedEffect(descriptionFieldFocused, amountFieldFocused) {
        if (amountFieldFocused) {
            if (descriptionFieldFocused) {
                keyboardController?.hide()
                delay(200)
                showCustomKeyboard = true
            } else {
                showCustomKeyboard = true
            }
        } else if (descriptionFieldFocused) {
            if (showCustomKeyboard) {
                showCustomKeyboard = false
                delay(200)
            } else {
                keyboardController?.show()
            }
        }
    }

    // Add BackHandler to handle back button press when custom keyboard is visible
    BackHandler(enabled = showCustomKeyboard) {
        showCustomKeyboard = false
        clearAmountFieldFocus = true
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = { selectedMillis ->
                selectedMillis?.let { millis ->
                    val newDate = Date(millis)
                    if (newDate.after(Date())) {
                        dateError = context.getString(R.string.date_error_while_choosing)
                    } else {
                        dateError = null
                        selectedDate = newDate
                    }
                }
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Optimized calculation function
    fun updateCalculation(newValue: String) {
        calculatedResult = keyboardHandler.calculateWithCaching(newValue, calculationPrefix)
    }

    // Save transaction function
    fun saveTransaction() {
        if (dateError == null) {
            val finalAmount = keyboardHandler.getFinalAmount(amountTextFieldValue, calculatedResult)

            viewModel.saveTransaction(
                customerId.toString(),
                finalAmount,
                description,
                transactionType == "You Got",
                date = selectedDate,
                navController
            )
        }
    }

    // Update transaction function
    fun updateTransaction() {
        if (dateError == null && customerTransactionEntity != null) {
            val finalAmount = keyboardHandler.getFinalAmount(amountTextFieldValue, calculatedResult)

            viewModel.updateTransaction(
                originalAmount = customerTransactionEntity!!.amount,
                amount = finalAmount,
                description = description,
                customerTransaction = customerTransactionEntity!!,
                navController = navController,
                date = selectedDate
            )
        }
    }

    Scaffold(
        topBar = {
            CustomTopBarWithIcon(
                title = if (transactionType == "You Gave") stringResource(R.string.you_gave) else stringResource(
                    R.string.you_got
                ),
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                CustomAmountTextField(
                    value = amountTextFieldValue,
                    modifier = Modifier.fillMaxWidth(),
                    onFocusChanged = { focused ->
                        amountFieldFocused = focused
                    },
                    shouldClearFocus = clearAmountFieldFocus,
                    onFocusCleared = {
                        clearAmountFieldFocus = false
                        amountFieldFocused = false
                    }
                )

                if (calculatedResult.isNotEmpty()) {
                    OutlinedTextField(
                        value = calculatedResult,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.calculation)) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            descriptionFieldFocused = focusState.isFocused
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date picker field
                OutlinedTextField(
                    value = dateFormatter.format(selectedDate),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.date)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                showDatePicker = true
                            }
                        },
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar Icon"
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown Icon"
                        )
                    },
                    isError = dateError != null,
                    supportingText = {
                        if (dateError != null) {
                            Text(
                                text = dateError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isEditing) {
                            updateTransaction()
                        } else {
                            saveTransaction()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = dateError == null
                ) {
                    Text(if (isEditing) stringResource(R.string.update) else stringResource(R.string.save))
                }
            }

            AnimatedVisibility(
                visible = showCustomKeyboard,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                CustomNumericKeyboard(
                    onDigitClicked = { digit ->
                        amountTextFieldValue = keyboardHandler.handleDigitInput(amountTextFieldValue, digit)
                        updateCalculation(amountTextFieldValue)
                    },
                    onClearClicked = {
                        amountTextFieldValue = ""
                        calculatedResult = ""
                        keyboardHandler.clearCache()
                    },
                    onBackspaceClicked = {
                        amountTextFieldValue = keyboardHandler.handleBackspace(amountTextFieldValue)
                        updateCalculation(amountTextFieldValue)
                    },
                    onOperatorClick = { operator ->
                        amountTextFieldValue = keyboardHandler.handleOperatorInput(amountTextFieldValue, operator)
                        updateCalculation(amountTextFieldValue)
                    },
                    onDecimalClicked = {
                        amountTextFieldValue = keyboardHandler.handleDecimalInput(amountTextFieldValue)
                        updateCalculation(amountTextFieldValue)
                    },
                    onPercentageClicked = {
                        amountTextFieldValue = keyboardHandler.handlePercentageInput(amountTextFieldValue)
                        updateCalculation(amountTextFieldValue)
                    },
                    onMemoryMinusClicked = {
                        // Implement memory functionality if needed
                    },
                    onMemoryPlusClicked = {
                        // Implement memory functionality if needed
                    }
                )
            }
        }
    }
}