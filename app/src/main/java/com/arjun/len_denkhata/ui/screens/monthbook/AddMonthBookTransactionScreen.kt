package com.arjun.len_denkhata.ui.screens.monthbook

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
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.R
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookExpenseCategory
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionEntity
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionType
import com.arjun.len_denkhata.data.utils.calculateExpression
import com.arjun.len_denkhata.data.utils.isExpression
import com.arjun.len_denkhata.monthbook.ui.viewmodel.MonthBookViewModel
import com.arjun.len_denkhata.ui.components.CustomAmountTextField
import com.arjun.len_denkhata.ui.components.CustomTopBarWithIcon
import com.arjun.len_denkhata.ui.components.DatePickerModal
import com.arjun.len_denkhata.ui.screens.CustomNumericKeyboard
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddMonthBookTransactionScreen(
    navController: NavHostController,
    transactionType: MonthBookTransactionType,
    viewModel: MonthBookViewModel,
    isEditing: Boolean = false,
    transactionId: Long = 0L
) {
    // State variables
    var amountTextFieldValue by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var calculatedResult by remember { mutableStateOf("") }
    var showCustomKeyboard by remember { mutableStateOf(false) }
    var amountFieldFocused by remember { mutableStateOf(false) }
    var descriptionFieldFocused by remember { mutableStateOf(false) }
    var selectedExpenseCategory by remember { mutableStateOf(MonthBookExpenseCategory.GENERAL) }
    var existingTransaction by remember { mutableStateOf<MonthBookTransactionEntity?>(null) }

    // Date handling
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Date()) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var dateError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Load existing transaction for editing
    LaunchedEffect(isEditing, transactionId) {
        if (isEditing && transactionId != 0L) {
            viewModel.getTransactionByIdOnce(transactionId)?.let { transaction ->
                existingTransaction = transaction
                amountTextFieldValue = transaction.amount.toString()
                description = transaction.description
                selectedExpenseCategory = transaction.expenseCategory ?: MonthBookExpenseCategory.GENERAL

                // Extract the Date part from the transaction's timestamp
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = transaction.timestamp
                }
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

                // Create a new Date object with only the date components
                val extractedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0) // Set time to the beginning of the day
                    set(Calendar.MILLISECOND, 0)
                }.time

                selectedDate = extractedDate
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

    // Save transaction function
    fun saveTransaction() {
        if (dateError == null) {
            val finalAmount = if (calculatedResult.isNotEmpty()) {
                calculateExpression(amountTextFieldValue)
            } else {
                amountTextFieldValue.toDoubleOrNull() ?: 0.0
            }

            viewModel.addTransaction(
                amount = finalAmount,
                description = description,
                type = transactionType,
                monthBookExpenseCategory = selectedExpenseCategory,
                date = selectedDate
            )
            navController.popBackStack()
        }
    }

    // Update transaction function
    fun updateTransaction() {
        if (dateError == null && existingTransaction != null) {
            val finalAmount = if (calculatedResult.isNotEmpty()) {
                calculateExpression(amountTextFieldValue)
            } else {
                amountTextFieldValue.toDoubleOrNull() ?: 0.0
            }

            viewModel.updateTransaction(
                existingTransaction = existingTransaction!!,
                amount = finalAmount,
                description = description,
                type = transactionType,
                monthBookExpenseCategory = selectedExpenseCategory,
                date = selectedDate
            )
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CustomTopBarWithIcon(
                title = if (transactionType == MonthBookTransactionType.INCOME) stringResource(R.string.add_income) else stringResource(
                    R.string.add_expense
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
                    modifier = Modifier
                        .fillMaxWidth(),
                    onFocusChanged = { focused ->
                        amountFieldFocused = focused
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

                if (transactionType == MonthBookTransactionType.EXPENSE) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.expense_category), style = MaterialTheme.typography.titleMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = selectedExpenseCategory == MonthBookExpenseCategory.GENERAL,
                            onClick = { selectedExpenseCategory = MonthBookExpenseCategory.GENERAL }
                        )
                        Text(stringResource(R.string.general))
                        RadioButton(
                            selected = selectedExpenseCategory == MonthBookExpenseCategory.WORK_RELATED,
                            onClick = { selectedExpenseCategory = MonthBookExpenseCategory.WORK_RELATED }
                        )
                        Text(stringResource(R.string.work_related))
                    }
                }

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
                        val newText = amountTextFieldValue + digit
                        amountTextFieldValue = newText
                        if (isExpression(newText)) {
                            calculatedResult = context.getString(R.string.calculated_amount) + calculateExpression(newText)
                        }
                    },
                    onClearClicked = {
                        amountTextFieldValue = ""
                        calculatedResult = ""
                    },
                    onBackspaceClicked = {
                        if (amountTextFieldValue.isNotEmpty()) {
                            val newText = amountTextFieldValue.dropLast(1)
                            amountTextFieldValue = newText
                            if (isExpression(newText)) {
                                calculatedResult = context.getString(R.string.calculated_amount) + calculateExpression(newText)
                            } else {
                                calculatedResult = ""
                            }
                        }
                    },
                    onOperatorClick = {
                        val newText = "$amountTextFieldValue$it"
                        amountTextFieldValue = newText
                        calculatedResult = context.getString(R.string.calculated_amount) + calculateExpression(newText)
                    },
                    onDecimalClicked = {
                        val newText = "$amountTextFieldValue."
                        amountTextFieldValue = newText
                        if (isExpression(newText)) {
                            calculatedResult = context.getString(R.string.calculated_amount) + calculateExpression(newText)
                        }
                    },
                    onPercentageClicked = {
                        val newText = "$amountTextFieldValue%"
                        amountTextFieldValue = newText
                        calculatedResult = context.getString(R.string.calculated_amount) + calculateExpression(newText)
                    },
                    onMemoryMinusClicked = {},
                    onMemoryPlusClicked = {}
                )
            }
        }
    }
}