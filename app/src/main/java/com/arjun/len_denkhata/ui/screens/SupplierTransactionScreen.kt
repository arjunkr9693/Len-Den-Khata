package com.arjun.len_denkhata.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.ui.viewmodel.TransactionViewModel

@Composable
fun SupplierTransactionScreen(navController: NavHostController, supplierId: String?) {
    val viewModel: TransactionViewModel = hiltViewModel()

    Scaffold(
        topBar = { TopAppBar(title = "Supplier Transactions", navController = navController, showBackButton = true) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            Text("Supplier Transactions for ID: $supplierId")
            // Add transaction list for supplier
        }
    }
}