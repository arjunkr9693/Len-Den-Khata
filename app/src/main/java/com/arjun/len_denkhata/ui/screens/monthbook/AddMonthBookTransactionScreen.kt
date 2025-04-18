package com.arjun.len_denkhata.ui.screens.monthbook

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookExpenseCategory
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionEntity
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionType
import com.arjun.len_denkhata.monthbook.ui.viewmodel.MonthBookViewModel
import com.arjun.len_denkhata.ui.components.CustomTopBarWithIcon
import com.arjun.len_denkhata.ui.screens.CustomNumericKeyboard
import java.util.*


@Composable
fun AddMonthBookTransactionScreen(
    navController: NavHostController,
    transactionType: MonthBookTransactionType,
    viewModel: MonthBookViewModel,
    isEditing: Boolean = false,
    transactionId: Long = 0L
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var calculatedResult by remember { mutableStateOf("") }
    var showCustomKeyboard by remember { mutableStateOf(false) }
    var selectedExpenseCategory by remember { mutableStateOf(MonthBookExpenseCategory.GENERAL) }
    var existingTransaction by remember { mutableStateOf<MonthBookTransactionEntity?>(null) }

    val context = LocalContext.current
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val keyboardVisible = remember { mutableStateOf(false) }
    val imePadding = WindowInsets.ime.asPaddingValues()
    val animatedPadding by animateDpAsState(
        targetValue = if (keyboardVisible.value) imePadding.calculateBottomPadding() else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )

    LaunchedEffect(isEditing, transactionId) {
        if (isEditing && transactionId != 0L) {
            viewModel.getTransactionByIdOnce(transactionId)?.let { transaction ->
                existingTransaction = transaction
                amount = transaction.amount.toString()
                description = transaction.description
                selectedExpenseCategory = transaction.expenseCategory ?: MonthBookExpenseCategory.GENERAL
            }
        }
    }

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
                val operand = parts.getOrNull(i+1)?.toDoubleOrNull() ?: 0.0

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

    fun saveTransaction() {
        val finalAmount = if (calculatedResult.isNotEmpty()) {
            calculateExpression(amount)
        } else {
            amount.toDoubleOrNull() ?: 0.0
        }

        viewModel.addTransaction(
            amount = finalAmount,
            description = description,
            type = transactionType,
            monthBookExpenseCategory = selectedExpenseCategory
        )
        navController.popBackStack()
    }

    fun updateTransaction() {
        val finalAmount = if (calculatedResult.isNotEmpty()) {
            calculateExpression(amount)
        } else {
            amount.toDoubleOrNull() ?: 0.0
        }

        if (existingTransaction != null) {
            viewModel.updateTransaction(
                existingTransaction = existingTransaction!!,
                amount = finalAmount,
                description = description,
                type = transactionType,
                monthBookExpenseCategory = selectedExpenseCategory
            )
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CustomTopBarWithIcon(
                title = if (transactionType == MonthBookTransactionType.INCOME) "Add Income" else "Add Expense",
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
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        amount = newValue
                        if (isExpression(newValue)) {
                            calculatedResult = "Calculated amount = ${calculateExpression(newValue)}"
                        } else {
                            calculatedResult = ""
                        }
                    },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                        .onFocusChanged { focusState ->
                            showCustomKeyboard = focusState.isFocused
                            keyboardVisible.value = focusState.isFocused
                            if (focusState.isFocused) {
                                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(view.windowToken, 0)
                            }
                        },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    readOnly = true
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
                    modifier = Modifier.fillMaxWidth()
                        .onFocusChanged { focusState ->
                            keyboardVisible.value = focusState.isFocused
                            showCustomKeyboard = false
                        }
                )

                if (transactionType == MonthBookTransactionType.EXPENSE) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Expense Category", style = MaterialTheme.typography.titleMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = selectedExpenseCategory == MonthBookExpenseCategory.GENERAL,
                            onClick = { selectedExpenseCategory = MonthBookExpenseCategory.GENERAL }
                        )
                        Text("General")
                        RadioButton(
                            selected = selectedExpenseCategory == MonthBookExpenseCategory.WORK_RELATED,
                            onClick = { selectedExpenseCategory = MonthBookExpenseCategory.WORK_RELATED }
                        )
                        Text("Work Related")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = animatedPadding)
            ) {
                Button(
                    onClick = {
                        if (isEditing) {
                            updateTransaction()
                        } else {
                            saveTransaction()
                        }
                        focusManager.clearFocus()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(if (isEditing) "Update" else "Save")
                }

                AnimatedVisibility(
                    visible = showCustomKeyboard,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CustomNumericKeyboard(
                        onDigitClicked = { digit ->
                            amount += digit
                            if (isExpression(amount)) {
                                calculatedResult = "Calculated amount = ${calculateExpression(amount)}"
                            }
                        },
                        onClearClicked = {
                            amount = ""
                            calculatedResult = ""
                        },
                        onBackspaceClicked = {
                            if (amount.isNotEmpty()) {
                                amount = amount.dropLast(1)
                                if (isExpression(amount)) {
                                    calculatedResult = "Calculated amount = ${calculateExpression(amount)}"
                                } else {
                                    calculatedResult = ""
                                }
                            }
                        },
                        onDivideClicked = {
                            amount += "/"
                            calculatedResult = "Calculated amount = ${calculateExpression(amount)}"
                        },
                        onMultiplyClicked = {
                            amount += "*"
                            calculatedResult = "Calculated amount = ${calculateExpression(amount)}"
                        },
                        onMinusClicked = {
                            amount += "-"
                            calculatedResult = "Calculated amount = ${calculateExpression(amount)}"
                        },
                        onPlusClicked = {
                            amount += "+"
                            calculatedResult = "Calculated amount = ${calculateExpression(amount)}"
                        },
                        onDecimalClicked = {
                            amount += "."
                            if (isExpression(amount)) {
                                calculatedResult = "Calculated amount = ${calculateExpression(amount)}"
                            }
                        },
                        onPercentageClicked = {
                            amount += "%"
                            calculatedResult = "Calculated amount = ${calculateExpression(amount)}"
                        },
                        onEqualsClicked = {
                            if (isExpression(amount)) {
                                val result = calculateExpression(amount)
                                amount = result.toString()
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
}