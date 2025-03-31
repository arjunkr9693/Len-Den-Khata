package com.arjun.len_denkhata.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomerTopBar(
    customerName: String, // Name of the customer
    customerPhoneNUmber: String,
    onBackClick: () -> Unit, // Callback for back navigation
    modifier: Modifier = Modifier,
    onTextClick: () -> Unit,

) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTextClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        // Customer Name
        Text(
            text = customerName,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        // Call Button
        IconButton(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$customerPhoneNUmber"))
                context.startActivity(intent)
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = "Call"
            )
        }
    }
}