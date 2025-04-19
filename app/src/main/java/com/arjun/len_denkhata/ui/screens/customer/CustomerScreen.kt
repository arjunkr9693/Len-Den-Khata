package com.arjun.len_denkhata.ui.screens.customer

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.arjun.len_denkhata.R
import com.arjun.len_denkhata.Screen
import com.arjun.len_denkhata.data.utils.CountryCodeDialog
import com.arjun.len_denkhata.data.utils.UserSession
import com.arjun.len_denkhata.ui.components.CustomerListItem
import com.arjun.len_denkhata.ui.components.RupeeCardRow
import com.arjun.len_denkhata.ui.screens.TopAppBar
import com.arjun.len_denkhata.ui.viewmodel.CustomerViewModel


@Composable
fun CustomerScreen(navController: NavHostController, viewModel: CustomerViewModel = hiltViewModel()) {
    var checkedContactName by remember { mutableStateOf(false) }

    val customers by viewModel.customers.collectAsState(initial = emptyList())
    val totalHaveToGive by viewModel.totalHaveToGive.collectAsState()
    val totalWillGet by viewModel.totalWillGet.collectAsState()
    val todayDue by viewModel.todayDue.collectAsState()

    val context = LocalContext.current;
    val showCountryCodeDialog by viewModel.showCountryCodeDialog.collectAsState()
    val phoneNumberToMerge by viewModel.phoneNumberToMerge.collectAsState()

    val showToast: (String) -> Unit = { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(customers.size){
        if(customers.isNotEmpty() && !checkedContactName) {
            viewModel.updateContactNamesFromPhonebook()
            checkedContactName = true
        }
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri = result.data?.data
            contactUri?.let { uri ->
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val name = it.getString(nameIndex)
                        val number = it.getString(numberIndex)

                        Log.d("testTag", "Name: $name, Number: $number")
                        viewModel.validateAndProcessContact(name, number, navController, showToast)
                    }
                }
            }
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            contactPickerLauncher.launch(intent)
        } else {
            // Handle permission denied
        }
    }


    Scaffold(
        topBar = { TopAppBar(stringResource(R.string.customers), navController = navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                UserSession.isContactPickerShowing = true
                when {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED -> {
                        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                        contactPickerLauncher.launch(intent)
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                }
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Customer")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            RupeeCardRow(totalCredit = totalHaveToGive, totalDebit = totalWillGet, todayDue = todayDue)

            LazyColumn{
                items(customers) {customer ->
                    CustomerListItem(customer = customer) { selectedCustomer ->
                        navController.navigate(Screen.CustomerTransaction.createRoute(selectedCustomer.id))
                    }
                }
            }
        }
        if (showCountryCodeDialog) {
            CountryCodeDialog(
                onConfirm = { countryCode ->
                    viewModel.mergeCountryCodeAndAddCustomer(countryCode, context.contentResolver, navController)
                },
                onDismiss = { viewModel.dismissCountryCodeDialog() }
            )
        }
    }
}