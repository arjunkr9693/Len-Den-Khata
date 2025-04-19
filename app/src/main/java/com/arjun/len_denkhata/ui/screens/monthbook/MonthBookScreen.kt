package com.arjun.len_denkhata.ui.screens.monthbook

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.R
import com.arjun.len_denkhata.Screen
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookExpenseCategory
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionEntity
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionType
import com.arjun.len_denkhata.monthbook.ui.viewmodel.MonthBookViewModel
import com.arjun.len_denkhata.ui.components.CustomTopBarWithIcon
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MonthBookScreen(
    navController: NavHostController,
    viewModel: MonthBookViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()

    Scaffold(
        topBar = {
            CustomTopBarWithIcon(
                title = stringResource(R.string.month_book),
                onBackClick = { navController.popBackStack() },
                onRightIconClick = { navController.navigate(Screen.MonthBookCalculatedData.route) },
                rightIcon = ImageVector.vectorResource(id = R.drawable.calculator_icon),
                onTitleClick = {}
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = { navController.navigate(Screen.AddIncome.route) }) {
                    Text(stringResource(R.string.add_income))
                }
                Button(onClick = { navController.navigate(Screen.AddExpense.route) }) {
                    Text(stringResource(R.string.add_expense))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                reverseLayout = false // Show latest at the bottom, like your example
            ) {
                val groupedTransactions = transactions.groupBy {
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(it.timestamp))
                }

                groupedTransactions.forEach { (date, transactionsForDate) ->
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = date,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }
                    }

                    items(transactionsForDate) { transaction ->
                        MonthBookTransactionItem(
                            transaction = transaction,
                            onDelete = viewModel::deleteTransaction,
                            onEdit = { transactionToEdit ->
                                navController.navigate(
                                    Screen.EditMonthBookTransaction.createRoute(
                                        transactionType = transactionToEdit.type.name.lowercase(),
                                        transactionId = transactionToEdit.id
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthBookTransactionItem(
    transaction: MonthBookTransactionEntity,
    onDelete: (MonthBookTransactionEntity) -> Unit,
    onEdit: (MonthBookTransactionEntity) -> Unit,
) {
    val formattedTimestamp = SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault()).format(Date(transaction.timestamp))
    var expanded by remember { mutableStateOf(false) }

    val backgroundColor = when (transaction.type) {
        MonthBookTransactionType.INCOME -> Color(0xFFE8F5E9) // Light green
        MonthBookTransactionType.EXPENSE -> Color(0xFFFFEBEE) // Light red
    }

    val transactionTypeText = if (transaction.type == MonthBookTransactionType.INCOME) stringResource(R.string.income) else stringResource(R.string.expense)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { /* Handle item click if needed */ },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formattedTimestamp,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = transaction.description,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    transaction.expenseCategory?.let {
                        Text(
                            text = stringResource(R.string.category) + ": " + if(it == MonthBookExpenseCategory.GENERAL) stringResource(R.string.general) else stringResource(R.string.work_related),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.amount_with_rupee, "%.2f".format(transaction.amount)),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.options))
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    onEdit(transaction)
                                    expanded = false
                                },
                                text = { Text(stringResource(R.string.edit)) }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    onDelete(transaction)
                                    expanded = false
                                },
                                text = { Text(stringResource(R.string.delete)) }
                            )
                        }
                    }
                    Text(
                        text = transactionTypeText,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}