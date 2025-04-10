package com.arjun.len_denkhata.ui.screens.monthbook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookExpenseCategory
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionEntity
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionType
import com.arjun.len_denkhata.monthbook.ui.viewmodel.MonthBookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMonthBookTransactionScreen(
    navController: NavHostController,
    transactionType: MonthBookTransactionType,
    viewModel: MonthBookViewModel,
    isEditing: Boolean = false,
    transactionId: Long = 0L // Receive transactionId for editing
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedExpenseCategory by remember { mutableStateOf(MonthBookExpenseCategory.GENERAL) } // Default

    var existingTransaction by remember { mutableStateOf<MonthBookTransactionEntity?>(null) }

    LaunchedEffect(isEditing, transactionId) {
        if (isEditing && transactionId != 0L) {
            viewModel.getTransactionByIdOnce(transactionId)?.let { transaction ->
                existingTransaction = transaction
                amount = transaction.amount.toString()
                description = transaction.description
                selectedExpenseCategory = transaction.expenseCategory ?: MonthBookExpenseCategory.GENERAL
            }
        } else {
            // Reset fields for adding new transaction
            amount = ""
            description = ""
            selectedExpenseCategory = MonthBookExpenseCategory.GENERAL
        }
    }

    fun saveTransaction() {
        val amountDouble = amount.toDoubleOrNull()
        if (amountDouble != null) {
            viewModel.addTransaction(
                amount = amountDouble,
                description = description,
                type = transactionType,
                monthBookExpenseCategory = selectedExpenseCategory
            )
            navController.popBackStack()
        }
    }

    fun updateTransaction() {
        val amountDouble = amount.toDoubleOrNull()
        if (amountDouble != null && existingTransaction != null) {
            viewModel.updateTransaction(
                existingTransaction = existingTransaction!!,
                amount = amountDouble,
                description = description,
                type = transactionType,
                monthBookExpenseCategory = selectedExpenseCategory
            )
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (transactionType == MonthBookTransactionType.INCOME) "Add Income" else "Add Expense") })
        },
        bottomBar = {
            Button(
                onClick = {
                    if (isEditing) {
                        updateTransaction()
                    } else {
                        saveTransaction()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Save")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            if (transactionType == MonthBookTransactionType.EXPENSE) {
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
        }
    }
}