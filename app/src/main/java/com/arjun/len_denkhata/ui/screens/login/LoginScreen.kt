package com.arjun.len_denkhata.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.R
import com.arjun.len_denkhata.ui.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController, viewModel: LoginViewModel = hiltViewModel()) {
    var mobileNumber by remember { mutableStateOf("") }
    var isErrorDialogOpen by remember { mutableStateOf(false) }
    val illegalPhoneNumberError = viewModel.illegalPhoneNumberError.collectAsState().value
    val context = LocalContext.current

    LaunchedEffect(illegalPhoneNumberError) {
        if (illegalPhoneNumberError.isNotEmpty()) {
            isErrorDialogOpen = true
        }
    }

    if (isErrorDialogOpen) {
        AlertDialog(
            onDismissRequest = { isErrorDialogOpen = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp) // Increased size for better visibility
                )
            },
            title = { Text(stringResource(R.string.invalid_phone_number)) },
            text = { Text(stringResource(R.string.phoneNumberInputInstruction)) },
            confirmButton = {
                TextButton(onClick = {
                    isErrorDialogOpen = false
                    viewModel.clearIllegalPhoneNumberError() // Clear the error in ViewModel
                }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Welcome Text
            Text(
                text = stringResource(R.string.welcome_to_app),
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary // Using your theme's primary color
                ),
                modifier = Modifier.padding(bottom = 32.dp, top = 64.dp) // Add some space below the title
            )

            // Mobile Number Input Field
            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                label = { Text(stringResource(R.string.mobile_number)) },
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Reduced width
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp), // Added rounded corners
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Login Button (now positioned in the middle vertically due to Arrangement.SpaceAround)
            Button(
                onClick = {
                    viewModel.login(mobileNumber, navController)
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(16.dp)
            ) {
                Text(stringResource(R.string.login))
            }
        }
    }
}