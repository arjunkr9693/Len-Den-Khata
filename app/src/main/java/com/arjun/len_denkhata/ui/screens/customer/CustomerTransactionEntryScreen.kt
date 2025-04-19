package com.arjun.len_denkhata.ui.screens.customer

import android.content.Context
import android.util.Log
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
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
    customerTransactionEntity: CustomerTransactionEntity?
) {
    var amountTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                customerTransactionEntity?.amount?.toString() ?: "",
                TextRange(customerTransactionEntity?.amount?.toString()?.length ?: 0)
            )
        )
    }
    var description by remember { mutableStateOf(customerTransactionEntity?.description ?: "") }
    var calculatedResult by remember { mutableStateOf("") }
    var showCustomKeyboard by remember { mutableStateOf(false) }
    var amountFieldFocused by remember { mutableStateOf(false) }
    var descriptionFieldFocused by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val view = LocalView.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

    // Keyboard visibility and padding
    val keyboardVisible = remember { mutableStateOf(false) }
    val imePadding = WindowInsets.ime.asPaddingValues()
    val animatedPadding by animateDpAsState(
        targetValue = if (keyboardVisible.value) imePadding.calculateBottomPadding() else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember {
        mutableStateOf(customerTransactionEntity?.date ?: Date())
    }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var dateError by remember { mutableStateOf<String?>(null) }

    // Set up keyboard visibility detection
    DisposableEffect(view) {
        val onGlobalListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            val keyboardVisibilityThreshold = screenHeight * 0.15
            keyboardVisible.value = keypadHeight > keyboardVisibilityThreshold
            showCustomKeyboard = amountFieldFocused && !keyboardVisible.value
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(onGlobalListener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalListener)
        }
    }

    // Cursor blinking animation
    val cursorBlinkState = remember { Animatable(0f) }
    LaunchedEffect(amountFieldFocused) {
        if (amountFieldFocused) {
            cursorBlinkState.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            cursorBlinkState.snapTo(0f)
        }
    }
    val cursorColor = if (cursorBlinkState.value > 0.5f && amountFieldFocused) LocalContentColor.current else Color.Transparent

    fun isExpression(input: String): Boolean {
        return input.contains("+") || input.contains("-") ||
                input.contains("*") || input.contains("/") ||
                input.contains("%")
    }

    fun calculateExpression(expression: String): Double {
        return try {
            val parts = expression.split("(?<=[+\\-*/%])|(?=[+\\-*/%])".toRegex())
            if (parts.size < 3) return expression.toDoubleOrNull() ?: 0.0

            var result = parts[0].toDoubleOrNull() ?: 0.0
            var i = 1
            while (i < parts.size) {
                val operator = parts[i]
                val operand = parts.getOrNull(i + 1)?.toDoubleOrNull() ?: 0.0

                when (operator) {
                    "+" -> result += operand
                    "-" -> result -= operand
                    "*" -> result *= operand
                    "/" -> if (operand != 0.0) result /= operand else return 0.0
                    "%" -> result %= operand
                }
                i += 2
            }
            result
        } catch (e: Exception) {
            0.0
        }
    }

    val amountFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }
    val dateFocusRequester = remember { FocusRequester() }

    // Handle focus and keyboard visibility
    LaunchedEffect(amountFieldFocused, descriptionFieldFocused) {
        if (amountFieldFocused) {
            delay(100)
            keyboardController?.hide()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        } else if (descriptionFieldFocused) {
            delay(100)
            keyboardController?.show()
        }
    }

    LaunchedEffect(Unit) {
        if (isEditing) {
            amountFocusRequester.requestFocus()
        }
    }


    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = { selectedMillis ->
                selectedMillis?.let { millis ->
                    val newDate = Date(millis)
                    if (newDate.after(Date())) {
                        dateError = "Mentioned date should be before the current date"
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
                title = if (transactionType == "You Gave") "You Gave" else "You Got",
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
                    onValueChange = { newValue ->
                        amountTextFieldValue = newValue
                        if (isExpression(newValue.text)) {
                            calculatedResult = "Calculated amount = ${calculateExpression(newValue.text)}"
                        } else {
                            calculatedResult = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(amountFocusRequester)
                        .onFocusChanged { focusState ->
                            amountFieldFocused = focusState.isFocused
                        },
                    showCursor = amountFieldFocused,
                    cursorColor = cursorColor
                )

                if (calculatedResult.isNotEmpty()) {
                    OutlinedTextField(
                        value = calculatedResult,
                        onValueChange = {},
                        label = { Text("Calculation") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(descriptionFocusRequester)
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
                    label = { Text("Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(dateFocusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                showDatePicker = true
                                focusManager.clearFocus()
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

                Spacer(modifier = Modifier.height(16.dp)) // Add some space below the date picker

                Button(
                    onClick = {
                        if (dateError == null) {
                            val finalAmount = if (calculatedResult.isNotEmpty()) {
                                calculateExpression(amountTextFieldValue.text)
                            } else {
                                amountTextFieldValue.text.toDoubleOrNull() ?: 0.0
                            }

                            if (isEditing && customerTransactionEntity != null) {
                                viewModel.updateTransaction(
                                    originalAmount = customerTransactionEntity.amount,
                                    amount = finalAmount,
                                    description = description,
                                    customerTransaction = customerTransactionEntity,
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
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = dateError == null
                ) {
                    Text("Save")
                }
            }

            AnimatedVisibility(
                visible = showCustomKeyboard,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = animatedPadding)
            ) {
                CustomNumericKeyboard(
                    onDigitClicked = { digit ->
                        val newText = amountTextFieldValue.text + digit
                        amountTextFieldValue = amountTextFieldValue.copy(
                            text = newText,
                            selection = TextRange(newText.length)
                        )
                        if (isExpression(newText)) {
                            calculatedResult = "Calculated amount = ${calculateExpression(newText)}"
                        }
                    },
                    onClearClicked = {
                        amountTextFieldValue = TextFieldValue("", TextRange(0))
                        calculatedResult = ""
                    },
                    onBackspaceClicked = {
                        if (amountTextFieldValue.text.isNotEmpty()) {
                            val newText = amountTextFieldValue.text.dropLast(1)
                            amountTextFieldValue = amountTextFieldValue.copy(
                                text = newText,
                                selection = TextRange(newText.length)
                            )
                            if (isExpression(newText)) {
                                calculatedResult = "Calculated amount = ${calculateExpression(newText)}"
                            } else {
                                calculatedResult = ""
                            }
                        }
                    },
                    onDivideClicked = {
                        val newText = amountTextFieldValue.text + "/"
                        amountTextFieldValue = amountTextFieldValue.copy(
                            text = newText,
                            selection = TextRange(newText.length)
                        )
                        calculatedResult = "Calculated amount = ${calculateExpression(newText)}"
                    },
                    onMultiplyClicked = {
                        val newText = amountTextFieldValue.text + "*"
                        amountTextFieldValue = amountTextFieldValue.copy(
                            text = newText,
                            selection = TextRange(newText.length)
                        )
                        calculatedResult = "Calculated amount = ${calculateExpression(newText)}"
                    },
                    onMinusClicked = {
                        val newText = amountTextFieldValue.text + "-"
                        amountTextFieldValue = amountTextFieldValue.copy(
                            text = newText,
                            selection = TextRange(newText.length)
                        )
                        calculatedResult = "Calculated amount = ${calculateExpression(newText)}"
                    },
                    onPlusClicked = {
                        val newText = amountTextFieldValue.text + "+"
                        amountTextFieldValue = amountTextFieldValue.copy(
                            text = newText,
                            selection = TextRange(newText.length)
                        )
                        calculatedResult = "Calculated amount = ${calculateExpression(newText)}"
                    },
                    onDecimalClicked = {
                        val newText = amountTextFieldValue.text + "."
                        amountTextFieldValue = amountTextFieldValue.copy(
                            text = newText,
                            selection = TextRange(newText.length)
                        )
                        if (isExpression(newText)) {
                            calculatedResult = "Calculated amount = ${calculateExpression(newText)}"
                        }
                    },
                    onPercentageClicked = {
                        val newText = amountTextFieldValue.text + "%"
                        amountTextFieldValue = amountTextFieldValue.copy(
                            text = newText,
                            selection = TextRange(newText.length)
                        )
                        calculatedResult = "Calculated amount = ${calculateExpression(newText)}"
                    },
                    onEqualsClicked = {
                        if (isExpression(amountTextFieldValue.text)) {
                            val result = calculateExpression(amountTextFieldValue.text)
                            val newText = result.toString()
                            amountTextFieldValue = amountTextFieldValue.copy(
                                text = newText,
                                selection = TextRange(newText.length)
                            )
                            calculatedResult = ""
                        }
                    },
                    onMemoryMinusClicked = {},
                    onMemoryPlusClicked = {}
                )
            }
        }
    }
}