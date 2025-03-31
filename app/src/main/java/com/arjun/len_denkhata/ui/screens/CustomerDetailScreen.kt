package com.arjun.len_denkhata.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.Screen
import com.arjun.len_denkhata.ui.viewmodel.CustomerViewModel

@Composable
fun CustomerDetailScreen(
    viewModel: CustomerViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val customer by viewModel.selectedCustomer.collectAsState()

    var name by remember { mutableStateOf(customer?.name) }
    var phone by remember { mutableStateOf(customer?.phone) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Name TextField with Save Button
        OutlinedTextField(
            value = name!!,
            onValueChange = { name = it },
            label = { Text("Name") },
            trailingIcon = {
                IconButton(
                    onClick = {
                        val updatedCustomer = name?.let { customer?.copy(name = it) }
                        if (updatedCustomer != null) {
                            viewModel.updateCustomer(updatedCustomer)
                        }
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Phone TextField
        OutlinedTextField(
            value = phone!!,
            readOnly = true,
            onValueChange = {},
            label = { Text("Phone") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Overall Balance Text
        Text(
            text = "Overall Balance: ${customer?.overallBalance}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            fontWeight = FontWeight.Bold
        )

        // Delete Button at the Bottom
        Button(
            onClick = {
                customer?.let { viewModel.deleteCustomer(it) }
                navController.navigate(Screen.Customer.route)
                      },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Delete Customer", color = Color.White)
        }
    }
}