package com.arjun.len_denkhata.ui.screens.monthbook

import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.monthbook.ui.viewmodel.MonthBookViewModel
import com.arjun.len_denkhata.ui.components.CustomTopBarWithIcon
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthBookCalculatedDataScreen(
    navController: NavHostController,
    viewModel: MonthBookViewModel = hiltViewModel()
) {
    val loading by viewModel.loadingCalculations.collectAsState()
    val totalIncome by viewModel.calculatedIncome.collectAsState()
    val totalExpense by viewModel.calculatedExpense.collectAsState()
    val netIncome by viewModel.calculatedNetIncome.collectAsState()
    val averageIncome by viewModel.calculatedAvgIncome.collectAsState()
    val averageIncomeOnIncomeDays by viewModel.calculatedAvgIncomeOnIncomeDays.collectAsState() // Observe the new StateFlow
    val expenseByCategory by viewModel.calculatedExpenseByCategory.collectAsState()
    val averageExpenseByCategory by viewModel.calculatedAvgExpenseByCategory.collectAsState()
    val monthlyTotals by viewModel.monthlyTotals.collectAsState()
    val currentMonth = YearMonth.now()

    LaunchedEffect(Unit) {
        viewModel.calculateMonthData()
    }

    Scaffold(
        topBar = {
            CustomTopBarWithIcon(
                title = "Month Book Details",
                onBackClick = { navController.popBackStack() },
                onRightIconClick = {},
                rightIcon = null,
                onTitleClick = {}
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Current Month (${currentMonth.format(DateTimeFormatter.ofPattern("MMMM '${if (Locale.getDefault().country == "IN") "₹" else ""}'", Locale.getDefault()))})",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("Income: ₹${String.format("%.2f", totalIncome)}", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Expense: ₹${String.format("%.2f", totalExpense)}", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Net Income: ₹${String.format("%.2f", netIncome)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Average Income: ₹${String.format("%.2f", averageIncome)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Average Income (Days with Income): ₹${String.format("%.2f", averageIncomeOnIncomeDays)}", // Display the new average
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Expenses by Category (Current Month)", style = MaterialTheme.typography.titleMedium)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        if (expenseByCategory.isNotEmpty()) {
                            expenseByCategory.forEach { (category, amount) ->
                                Text("$category: ₹${String.format("%.2f", amount)}", style = MaterialTheme.typography.bodyLarge)
                            }
                        } else {
                            Text("No expenses recorded this month.", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Average Expense by Category (Current Month)", style = MaterialTheme.typography.titleMedium)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        if (averageExpenseByCategory.isNotEmpty()) {
                            averageExpenseByCategory.forEach { (category, amount) ->
                                Text("$category: ₹${String.format("%.2f", amount)}", style = MaterialTheme.typography.bodyLarge)
                            }
                        } else {
                            Text("No expenses recorded this month.", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Add some space before the monthly totals
            Text("Monthly Income & Expense", style = MaterialTheme.typography.headlineSmall)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    monthlyTotals.forEach { (yearMonth, totals) ->
                        val formattedMonth = yearMonth.format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault()))
                        Text(
                            "$formattedMonth - Expense: ₹${String.format("%.2f", totals.second)}, Income: ₹${String.format("%.2f", totals.first)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

