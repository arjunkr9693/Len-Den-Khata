package com.arjun.len_denkhata.ui.screens.customer

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.arjun.len_denkhata.ui.components.CustomTopBarWithIcon
import com.arjun.len_denkhata.ui.screens.CustomNumericKeyboard
import com.arjun.len_denkhata.ui.viewmodel.CustomerTransactionEntryViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerTransactionEntryScreen(
    navController: NavHostController,
    customerId: String?,
    transactionType: String,
    viewModel: CustomerTransactionEntryViewModel = hiltViewModel(),
    isEditing: Boolean = false,
    customerTransactionEntity: CustomerTransactionEntity?
) {
    var amount by remember { mutableStateOf(customerTransactionEntity?.amount?.toString() ?: "") }
    var description by remember { mutableStateOf(customerTransactionEntity?.description ?: "") }
    var calculatedResult by remember { mutableStateOf("") }
    var showCustomKeyboard by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val view = LocalView.current

    fun isExpression(input: String): Boolean {
        return input.contains("+") || input.contains("-") ||
                input.contains("*") || input.contains("/") ||
                input.contains("%")
    }

    fun calculateExpression(expression: String): Double {
        return try {
            // Simple calculation without external library
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

    val keyboardVisible = remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val imePadding = WindowInsets.ime.asPaddingValues()
    val animatedPadding by animateDpAsState(
        targetValue = if (keyboardVisible.value) imePadding.calculateBottomPadding() else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )

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
                        val finalAmount = if (calculatedResult.isNotEmpty()) {
                            calculateExpression(amount)
                        } else {
                            amount.toDoubleOrNull() ?: 0.0
                        }

                        if (isEditing) {
                            viewModel.updateTransaction(
                                originalAmount = customerTransactionEntity!!.amount,
                                amount = finalAmount,
                                description = description,
                                customerTransaction = customerTransactionEntity,
                                navController = navController
                            )
                        } else {
                            viewModel.saveTransaction(
                                customerId.toString(),
                                finalAmount,
                                description,
                                transactionType == "You Got",
                                Date(),
                                navController
                            )
                        }
                        focusManager.clearFocus()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Save")
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