package com.arjun.len_denkhata.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.arjun.len_denkhata.ui.viewmodel.CustomerTransactionEntryViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerTransactionEntryScreen(
    navController: NavHostController,
    customerId: String?,
    transactionType: String, // "You Gave" or "You Got"
    viewModel: CustomerTransactionEntryViewModel = hiltViewModel(),
    isEditing: Boolean = false,
    customerTransactionEntity: CustomerTransactionEntity?
) {
    var amount by remember { mutableStateOf(customerTransactionEntity?.amount?.toString() ?: "")}
    var description by remember { mutableStateOf(customerTransactionEntity?.description ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (transactionType == "You Gave") "You Gave" else "You Got") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(androidx.compose.material.icons.Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if(isEditing) {
                        viewModel.saveTransaction(
                            isEditing = true,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            description = description,
                            customerTransaction = customerTransactionEntity!!,
                            navController = navController
                        )
                    }else {
                        Log.d("testTag", customerId.toString())
                        viewModel.saveTransaction(
                            customerId.toString(),
                            amount.toDoubleOrNull() ?: 0.0,
                            description,
                            transactionType == "You Got", // isCredit
                            Date(),
                            navController,
                        )
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
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}