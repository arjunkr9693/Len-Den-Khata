package com.arjun.len_denkhata.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CustomClickableRoundedBox(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String
) {
    val buttonColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(
                color = buttonColor,
                shape = RoundedCornerShape(8.dp) // Adjust corner radius as needed
            )
            .padding(horizontal = 16.dp, vertical = 8.dp), // Add padding for better touch target
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodyMedium // Use appropriate text style
        )
    }
}