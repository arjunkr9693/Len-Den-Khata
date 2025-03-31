package com.arjun.len_denkhata.data.utils

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun CountryCodeDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var countryCode by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Enter Country Code") },
        text = { TextField(value = countryCode, onValueChange = { countryCode = it }, label = { Text("Country Code") }) },
        confirmButton = {
            Button(onClick = { onConfirm(countryCode) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}