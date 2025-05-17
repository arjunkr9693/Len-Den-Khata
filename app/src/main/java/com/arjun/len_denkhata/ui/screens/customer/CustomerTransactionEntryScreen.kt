package com.arjun.len_denkhata.ui.screens.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.arjun.len_denkhata.data.utils.calculateExpression
import com.arjun.len_denkhata.data.utils.isExpression
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
    val customerTransactionEntity by viewModel.transactionForEdit.collectAsState()

    LaunchedEffect(transactionId) {
        if (transactionId != -1L && isEditing) {
            viewModel.loadTransactionByTransactionId(transactionId)
        }
    }

    var amountTextFieldValue by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var calculatedResult by remember { mutableStateOf("") }
    var showCustomKeyboard by remember { mutableStateOf(false) }
    var amountFieldFocused by remember { mutableStateOf(false) }
    var descriptionFieldFocused by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Date()) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var dateError by remember { mutableStateOf<String?>(null) }


    // Watch for changes to customerTransactionEntity and update UI accordingly
    LaunchedEffect(customerTransactionEntity) {
        customerTransactionEntity?.let { entity ->
            amountTextFieldValue = entity.amount.toString()
            description = entity.description ?: ""
            selectedDate = entity.date
        } ?: run {
            if (!isEditing) {
                amountTextFieldValue = ""
                description = ""
                selectedDate = Date()
            }
        }
    }

    LaunchedEffect(amountFieldFocused) {
        showCustomKeyboard = amountFieldFocused
    }

    // Handle focus and keyboard visibility
    LaunchedEffect(descriptionFieldFocused, amountFieldFocused) {
        if (amountFieldFocused) {
            if(descriptionFieldFocused) {
                keyboardController?.hide()
                delay(200)
                showCustomKeyboard = true
            }else {
                showCustomKeyboard = true
            }
        }
        else if(descriptionFieldFocused) {
            if (showCustomKeyboard) {
                showCustomKeyboard = false
                delay(200)
            }else {
                keyboardController?.show()
            }

        }
    }


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
                        if (dateError == null) {
                            val finalAmount = if (calculatedResult.isNotEmpty()) {
                                calculateExpression(amountTextFieldValue)
                            } else {
                                amountTextFieldValue.toDoubleOrNull() ?: 0.0
                            }

                            if (isEditing && customerTransactionEntity != null) {
                                viewModel.updateTransaction(
                                    originalAmount = customerTransactionEntity!!.amount,
                                    amount = finalAmount,
                                    description = description,
                                    customerTransaction = customerTransactionEntity!!,
                                    navController = navController,
                                    date = selectedDate
                                )
                            } else {
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