package com.arjun.len_denkhata.ui.screens.monthbook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.R
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookExpenseCategory
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
                title = stringResource(R.string.month_book_details),
                onBackClick = { navController.popBackStack() },
                onRightIconClick = {},
                rightIcon = null,
                onTitleClick = {}
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(0.2f),
                    strokeWidth = 4.dp
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.current_month_with_amount, currentMonth.format(DateTimeFormatter.ofPattern("MMMM${if (Locale.getDefault().country == "IN") "₹" else ""}", Locale.getDefault()))),
                                style = MaterialTheme.typography.headlineSmall
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Text(stringResource(R.string.income_with_amount, String.format("%.2f", totalIncome)), style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.expense_with_amount, String.format("%.2f", totalExpense)), style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.net_income_with_amount, String.format("%.2f", netIncome)),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.average_income_with_amount, String.format("%.2f", averageIncome)),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.average_income_on_income_days_with_amount, String.format("%.2f", averageIncomeOnIncomeDays)), // Display the new average
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.expenses_by_category_current_month), style = MaterialTheme.typography.titleMedium)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            if (expenseByCategory.isNotEmpty()) {
                                expenseByCategory.forEach { (category, amount) ->
                                    val expenseText = if (category == MonthBookExpenseCategory.GENERAL ) stringResource(R.string.general) else stringResource(R.string.work_related)
                                    val textToShow = expenseText + ": " + String.format("%.2f", amount)
                                    Text(
                                        text = textToShow,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            } else {
                                Text(stringResource(R.string.no_expenses_recorded_this_month), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.average_expense_by_category_current_month), style = MaterialTheme.typography.titleMedium)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            if (averageExpenseByCategory.isNotEmpty()) {
                                averageExpenseByCategory.forEach { (category, amount) ->
                                    val expenseText = if (category == MonthBookExpenseCategory.GENERAL ) stringResource(R.string.general) else stringResource(R.string.work_related)
                                    val textToshow = expenseText + ": " + String.format("%.2f", amount)
                                    Text( textToshow , style = MaterialTheme.typography.bodyLarge)
                                }
                            } else {
                                Text(stringResource(R.string.no_expenses_recorded_this_month), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp)) // Add some space before the monthly totals
                    Text(stringResource(R.string.monthly_income_expense), style = MaterialTheme.typography.headlineSmall)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)) {
                            monthlyTotals.forEach { (yearMonth, totals) ->
                                val formattedMonth = yearMonth.format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault()))
                                Text(
                                    stringResource(R.string.month_expense_income, formattedMonth, String.format("%.2f", totals.second), String.format("%.2f", totals.first)),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                }

        }
    }
}