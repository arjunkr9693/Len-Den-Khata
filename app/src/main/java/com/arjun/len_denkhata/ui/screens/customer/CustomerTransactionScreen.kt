package com.arjun.len_denkhata.ui.screens.customer

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.R
import com.arjun.len_denkhata.Screen
import com.arjun.len_denkhata.data.database.TransactionUiModel
import com.arjun.len_denkhata.ui.components.CustomClickableRoundedBox
import com.arjun.len_denkhata.ui.components.CustomTopBarWithIcon
import com.arjun.len_denkhata.ui.components.SearchBar
import com.arjun.len_denkhata.ui.components.TransactionItem
import com.arjun.len_denkhata.ui.viewmodel.CustomerViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomerTransactionScreen(
    navController: NavHostController,
    customerId: String,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val filteredGroupedTransactions by viewModel.filteredGroupedTransactions.collectAsState()
    val customer by viewModel.selectedCustomer.collectAsState()
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<TransactionUiModel?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadTransactions(customerId)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.confirm_deletion)) },
            text = { Text(stringResource(R.string.are_you_sure_delete_transaction)) },
            icon = { Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete)) },
            confirmButton = {
                TextButton(onClick = {
                    transactionToDelete?.let { viewModel.deleteTransaction(it.id) }
                    showDialog = false
                    transactionToDelete = null
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
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
                CustomClickableRoundedBox(
                    onClick = {
                        navController.navigate(
                            Screen.TransactionEntry.createRoute(
                                customerId = customerId,
                                transactionType = "You Got"
                            )
                        )
                    },
                    text = stringResource(R.string.you_got),
                    modifier = Modifier.height(48.dp))

                CustomClickableRoundedBox(onClick = {
                        navController.navigate(
                            Screen.TransactionEntry.createRoute(
                                customerId = customerId,
                                transactionType = "You Gave"
                            )
                        )
                    },
                    text = stringResource(R.string.you_gave),
                    modifier = Modifier.height(48.dp)
                )
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
                        stringResource(R.string.you_will_get) + customer!!.overallBalance.toString()
                    } else {
                        stringResource(R.string.you_will_give) + (customer!!.overallBalance * -1).toString()
                    }
                } else {
                    stringResource(R.string.you_will_get) + "0.0"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                fontWeight = FontWeight.Bold
            )

            SearchBar(
                query = viewModel.searchQuery.collectAsState().value,
                onQueryChange = { viewModel.setSearchQuery(it) },
                placeholder = stringResource(R.string.search_amount_description)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = rememberLazyListState(),
            ) {
                filteredGroupedTransactions.forEach { (date, transactionsForDate) ->
                    stickyHeader {
                        Text(
                            text = date,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    items(
                        items = transactionsForDate,
                        key = { it.id }
                    ) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onDelete = {
                                transactionToDelete = transaction
                                showDialog = true
                            },
                            onEdit = { transactionId, isCredit ->
                                navController.navigate(
                                    Screen.TransactionEntry.createRoute(
                                        customerId = customerId,
                                        transactionType = if (isCredit) "You Got" else "You Gave",
                                        isEditing = true,
                                        transactionId = transactionId
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