package com.arjun.len_denkhata.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.arjun.len_denkhata.ui.viewmodel.TransactionViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun TransactionScreen(navController: NavHostController, customerId: Int?, type: String?) {
    val viewModel: TransactionViewModel = hiltViewModel()

    Scaffold(
        topBar = { TopAppBar(title = if (type == "customer") "Customer Transactions" else "Supplier Transactions", navController = navController, showBackButton = true) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            if (type == "customer" && customerId != null) {
                Text("Customer Transactions for ID: $customerId")
                // Add transaction list for customer
            } else {
                Text("Invalid transaction type or ID")
            }
        }
    }
}