package com.arjun.len_denkhata.ui.screens.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.Screen
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.arjun.len_denkhata.ui.components.CustomTopBarWithIcon
import com.arjun.len_denkhata.ui.viewmodel.CustomerViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomerTransactionScreen(
    navController: NavHostController,
    customerId: String,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactionsByCustomer.collectAsState()
    val customer by viewModel.selectedCustomer.collectAsState()
    val context = LocalContext.current

    val groupedTransactions = remember(transactions) {
        transactions.groupBy {
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it.date)
        }.toSortedMap(reverseOrder()) // Ensure dates are in descending order
    }

    Scaffold(
        topBar = {
            customer?.let {
                CustomTopBarWithIcon(
                    title = it.name,
                    onBackClick = { navController.popBackStack() },
                    onTitleClick = { navController.navigate("customerDetail") },
                    rightIcon = Icons.Default.Call,
                    onRightIconClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${it.phone}"))
                        context.startActivity(intent)
                    }
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = {
                    navController.navigate(
                        Screen.TransactionEntry.createRoute(
                            customerId = customerId,
                            transactionType = "You Gave"
                        )
                    )
                }) {
                    Text("You Gave")
                }
                Button(onClick = {
                    navController.navigate(
                        Screen.TransactionEntry.createRoute(
                            customerId = customerId,
                            transactionType = "You Got"
                        )
                    )
                }) {
                    Text("You Got")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Text(
                text = if (customer != null) {
                    if (customer!!.overallBalance >= 0) {
                        "You will get: ${customer!!.overallBalance}"
                    } else {
                        "You will give: ${customer!!.overallBalance * -1}"
                    }
                } else {
                    "You will get: 0.0"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
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
                        TransactionItem(
                            transaction = transaction,
                            onDelete = { viewModel.deleteTransaction(it, deletedAmount = it.amount) },
                            onEdit = {
                                navController.navigate(
                                    Screen.TransactionEntry.createRoute(
                                        customerId = customerId,
                                        transactionType = if (it.isCredit) "You Gave" else "You Got",
                                        isEditing = true,
                                        customerTransaction = it
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(customerId) {
        viewModel.loadTransactions(customerId)
        viewModel.loadCustomerBalance(customerId)
    }
}

@Composable
fun TransactionItem(
    transaction: CustomerTransactionEntity,
    onDelete: (CustomerTransactionEntity) -> Unit,
    onEdit: (CustomerTransactionEntity) -> Unit,
) {
    val formattedTimestamp =
        SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault()).format(Date(transaction.timestamp))
    var expanded by remember { mutableStateOf(false) } // State to manage dropdown menu visibility

    // Determine if the transaction is from the owner or another user
    val isMadeByOwner = transaction.isMadeByOwner

    // Determine if the transaction is credit or debit based on the owner's perspective
    val isCredit = transaction.isCredit

    // Background color based on debit/credit
    val backgroundColor = if (isCredit) Color(0xFFE8F5E9) else Color(0xFFFFEBEE) // Light green for credit, light red for debit

    // Text for "You gave" or "You got"
    val transactionTypeText = if (isCredit) "You got" else "You gave"

    // Alignment based on owner or other user
    val alignment = if (isMadeByOwner) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { },
        horizontalArrangement = alignment
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 350.dp)
                .clickable {  }, // Limit width for better readability
            elevation = CardDefaults.cardElevation(5.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor, shape = RoundedCornerShape(8.dp)),
                horizontalArrangement = Arrangement.Absolute.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(0.7f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = formattedTimestamp,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    // Description below the timestamp
                    transaction.description?.let {
                        Text(
                            text = it,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier.padding(end = 8.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Menu bar at the top right (only for owner's transactions)
                    if (isMadeByOwner) {
                        Row(
                            modifier = Modifier
                        ) {
                            IconButton(
                                onClick = { expanded = true },
                                Modifier.fillMaxSize(0.2f)
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Options")
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
                                    text = {
                                        Text("Edit")
                                    }
                                )
                                DropdownMenuItem(
                                    onClick = {
                                        onDelete(transaction)
                                        expanded = false
                                    },
                                    text = {
                                        Text("Delete")
                                    }
                                )
                            }
                        }
                    }
                    // Amount
                    Text(
                        text = "₹${"%.2f".format(transaction.amount)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // "You gave" or "You got" text
                    Text(
                        text = transactionTypeText,
                        fontSize = 10.sp, // Increased font size for better visibility
                        color = Color.Gray,
                    )
                }
            }
        }
    }
}