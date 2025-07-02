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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.R
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
    // Observe ViewModel state
    val amountTextFieldValue by viewModel.amountTextFieldValue.collectAsState()
    val calculatedResult by viewModel.calculatedResult.collectAsState()
    val description by viewModel.description.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dateError by viewModel.dateError.collectAsState()
    val customerTransactionEntity by viewModel.transactionForEdit.collectAsState()

    // State for keyboard visibility
    var showCustomKeyboard by remember { mutableStateOf(false) }
    var amountFieldFocused by remember { mutableStateOf(false) }
    var descriptionFieldFocused by remember { mutableStateOf(false) }
    var clearAmountFieldFocus by remember { mutableStateOf(false) }

    // Date handling
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val keyboardController = LocalSoftwareKeyboardController.current

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
                        if (isEditing && customerTransactionEntity != null) {
                            viewModel.updateTransaction(
                                originalAmount = customerTransactionEntity!!.amount,
                                customerTransaction = customerTransactionEntity!!,
                                navController = navController
                            )
                        } else {
                            viewModel.saveTransaction(
                                customerId = customerId.toString(),
                                isCredit = transactionType == "You Got",
                                navController = navController
                            )
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