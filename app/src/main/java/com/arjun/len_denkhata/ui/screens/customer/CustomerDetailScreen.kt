package com.arjun.len_denkhata.ui.screens.customer

import android.widget.Toast
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.R
import com.arjun.len_denkhata.Screen
import com.arjun.len_denkhata.ui.components.CustomTopBarWithIcon
import com.arjun.len_denkhata.ui.viewmodel.CustomerViewModel

@Composable
fun CustomerDetailScreen(
    viewModel: CustomerViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val customer by viewModel.selectedCustomer.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf(customer?.name) }
    var phone by remember { mutableStateOf(customer?.phone) }

    Scaffold(
        topBar = {
            CustomTopBarWithIcon(
                title = stringResource(R.string.customer_details),
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp) // Horizontal padding of 12.dp
                .padding(paddingValues)         // Apply vertical padding from Scaffold
        ) {
            // Name TextField with Save Button
            OutlinedTextField(
                value = name!!,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name)) },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val updatedCustomer = name?.let { customer?.copy(name = it) }
                            if (updatedCustomer != null) {
                                viewModel.updateCustomer(updatedCustomer)
                                Toast.makeText(context, "Customer updated successfully", Toast.LENGTH_SHORT).show()                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save))
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
                label = { Text(stringResource(R.string.phone)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Overall Balance Text
            Text(
                text = stringResource(R.string.overall_balance) + customer?.overallBalance.toString(),
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
                Text(stringResource(R.string.delete_customer), color = Color.White)
            }
        }
    }
}