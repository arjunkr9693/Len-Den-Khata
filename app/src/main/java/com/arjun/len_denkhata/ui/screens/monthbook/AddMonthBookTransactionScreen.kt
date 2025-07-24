package com.arjun.len_denkhata.ui.screens.monthbook

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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.R
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookExpenseCategory
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionType
import com.arjun.len_denkhata.ui.viewmodel.MonthBookViewModel
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
    viewModel: MonthBookViewModel = hiltViewModel(),
    isEditing: Boolean = false,
    transactionId: Long = 0L
) {
    // Observe ViewModel state
    val amountTextFieldValue by viewModel.amountTextFieldValue.collectAsState()
    val calculatedResult by viewModel.calculatedResult.collectAsState()
    val description by viewModel.description.collectAsState()
    val selectedExpenseCategory by viewModel.selectedExpenseCategory.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dateError by viewModel.dateError.collectAsState()
    val transactionForEdit by viewModel.transactionForEdit.collectAsState()

    // State for keyboard visibility
    var showCustomKeyboard by remember { mutableStateOf(false) }
    var amountFieldFocused by remember { mutableStateOf(false) }
    var descriptionFieldFocused by remember { mutableStateOf(false) }
    var clearAmountFieldFocus by remember { mutableStateOf(false) }
    var forceHideSystemKeyboard by remember { mutableStateOf(false) }

    // Date handling
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current

    // Load transaction for editing
    LaunchedEffect(isEditing, transactionId) {
        if (isEditing && transactionId != 0L) {
            viewModel.loadTransactionById(transactionId)
        }
    }

    // Force hide system keyboard when needed
    LaunchedEffect(forceHideSystemKeyboard) {
        if (forceHideSystemKeyboard) {
            // Use InputMethodManager to force hide keyboard
            val imm = view.context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                    as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)

            // Also try with keyboard controller
            keyboardController?.hide()

            // Reset the flag
            forceHideSystemKeyboard = false
        }
    }

    // Handle keyboard visibility with improved logic
    LaunchedEffect(amountFieldFocused, descriptionFieldFocused) {
        when {
            amountFieldFocused -> {
                // Amount field is focused - force hide system keyboard and show custom keyboard
                forceHideSystemKeyboard = true
                delay(150) // Wait for system keyboard to hide
                showCustomKeyboard = true
            }
            descriptionFieldFocused -> {
                // Description field is focused - hide custom keyboard and show system keyboard
                showCustomKeyboard = false
                delay(200) // Wait for custom keyboard to hide
                keyboardController?.show()
            }
            else -> {
                // No field is focused - hide all keyboards
                showCustomKeyboard = false
                forceHideSystemKeyboard = true
            }
        }
    }

    LaunchedEffect(transactionType) {
        if (!isEditing) {
            viewModel.loadUnsavedTransaction(transactionType)
        }
    }

    // Save unsaved transaction when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            if (!isEditing) {
                viewModel.saveUnsavedTransaction(transactionType)
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
                    viewModel.updateDate(Date(millis))
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Scaffold(
        topBar = {
            CustomTopBarWithIcon(
                title = if (transactionType == MonthBookTransactionType.INCOME)
                    stringResource(R.string.add_income) else stringResource(R.string.add_expense),
                onBackClick = {
                    if (!isEditing) {
                        viewModel.saveUnsavedTransaction(transactionType)
                    }
                    navController.popBackStack()
                }
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
                    onValueChange = { viewModel.updateDescription(it) },
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
                            onClick = { viewModel.updateExpenseCategory(MonthBookExpenseCategory.GENERAL) }
                        )
                        Text(stringResource(R.string.general))
                        RadioButton(
                            selected = selectedExpenseCategory == MonthBookExpenseCategory.WORK_RELATED,
                            onClick = { viewModel.updateExpenseCategory(MonthBookExpenseCategory.WORK_RELATED) }
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
                        dateError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isEditing && transactionForEdit != null) {
                            viewModel.updateTransaction(
                                existingTransaction = transactionForEdit!!,
                                amount = 0.0, // Will be overridden by getFinalAmount
                                description = description,
                                type = transactionType,
                                monthBookExpenseCategory = if (transactionType == MonthBookTransactionType.EXPENSE) selectedExpenseCategory else null,
                                date = selectedDate
                            )
                            navController.popBackStack()
                        } else {
                            viewModel.addTransaction(
                                amount = 0.0, // Will be overridden by getFinalAmount
                                description = description,
                                type = transactionType,
                                monthBookExpenseCategory = if (transactionType == MonthBookTransactionType.EXPENSE) selectedExpenseCategory else null,
                                date = selectedDate
                            )
                            navController.popBackStack()
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
                        viewModel.handleDigitInput(digit)
                    },
                    onClearClicked = {
                        viewModel.clearInput()
                    },
                    onBackspaceClicked = {
                        viewModel.handleBackspace()
                    },
                    onOperatorClick = { operator ->
                        viewModel.handleOperatorInput(operator)
                    },
                    onDecimalClicked = {
                        viewModel.handleDecimalInput()
                    },
                    onPercentageClicked = {
                        viewModel.handlePercentageInput()
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