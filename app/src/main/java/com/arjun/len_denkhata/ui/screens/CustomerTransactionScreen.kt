package com.arjun.len_denkhata.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.arjun.len_denkhata.data.utils.UserSession
import com.arjun.len_denkhata.ui.components.CustomerTopBar
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

    // Create and remember the LazyListState
    val listState = rememberLazyListState()

    // Track if we should auto-scroll to the top
    var shouldAutoScroll by remember { mutableStateOf(true) }

    // Track the previous transaction count to detect new additions
    var previousTransactionCount by remember { mutableIntStateOf(0) }
    val currentTransactionCount = transactions.size

    // Effect to handle auto-scrolling behavior
    LaunchedEffect(currentTransactionCount) {
        if (currentTransactionCount > previousTransactionCount) {
            if (shouldAutoScroll) {
                // Scroll to the top when new items are added
                listState.animateScrollToItem(index = 0)
            }
            previousTransactionCount = currentTransactionCount
        }
    }

    // Effect to detect user scrolling and disable auto-scroll when they scroll down
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            // Check if user is not at the top
            val isAtTop = listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0
            shouldAutoScroll = isAtTop
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadTransactions(customerId)
        viewModel.loadCustomerBalance(customerId)
    }

    Scaffold(
        topBar = {
            customer?.let {
                CustomerTopBar(
                    customerName = it.name,
                    customerPhoneNUmber = it.phone,
                    onBackClick = { navController.popBackStack() },
                    onTextClick = { navController.navigate("customerDetail") }
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
                    navController.navigate(Screen.TransactionEntry.createRoute(
                        customerId = customerId,
                        transactionType = "You Gave"
                    ))
                }) {
                    Text("You Gave")
                }
                Button(onClick = {
                    navController.navigate(Screen.TransactionEntry.createRoute(
                        customerId = customerId,
                        transactionType = "You Got"
                    ))
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
                state = listState,
                modifier = Modifier.weight(1f),
                reverseLayout = true // This reverses the scroll direction
            ) {
                val groupedTransactions = transactions.groupBy {
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it.date)
                }

                // Reverse the order of groups to show recent first
                val reversedGroups = groupedTransactions.entries

                reversedGroups.forEach { (date, transactionsForDate) ->
                    item {
                        Text(
                            text = date,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Reverse the transactions within each date group
                    items(transactionsForDate) { transaction ->
                        TransactionItem(
                            transaction,
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
}

@Composable
fun TransactionItem(
    transaction: CustomerTransactionEntity,
    onDelete: (CustomerTransactionEntity) -> Unit,
    onEdit: (CustomerTransactionEntity) -> Unit,
) {
    val formattedTimestamp = SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault()).format(Date(transaction.timestamp))
    var expanded by remember { mutableStateOf(false) } // State to manage dropdown menu visibility

    // Determine if the transaction is from the owner or another user
    val isOwner = transaction.ownerId == UserSession.phoneNumber

    // Determine if the transaction is credit or debit based on the owner's perspective
    val isCredit = if (isOwner) transaction.isCredit else !transaction.isCredit

    // Background color based on debit/credit
    val backgroundColor = if (isCredit) Color(0xFFE8F5E9) else Color(0xFFFFEBEE) // Light green for credit, light red for debit

    // Text for "You gave" or "You got"
    val transactionTypeText = if (isCredit) "You got" else "You gave"

    // Alignment based on owner or other user
    val alignment = if (isOwner) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable {  },
        horizontalArrangement = alignment
    ) {
        Card(
            modifier = Modifier
                .widthIn( max = 350.dp) // Limit width for better readability
        ) {
            Row (modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor, shape = RoundedCornerShape(8.dp)),
                horizontalArrangement = Arrangement.Absolute.SpaceBetween
            ){
                Column(modifier = Modifier
                    .fillMaxSize(0.7f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)) {
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
                    if (isOwner) {
                        Row(
                            modifier = Modifier
                        ) {
                            IconButton(onClick = { expanded = true },
                                Modifier.fillMaxSize(0.2f)) {
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
                        fontSize = 6.sp,
                        color = Color.Gray,
                    )
                }
            }

            // Amount and transaction type text

        }


    }
}