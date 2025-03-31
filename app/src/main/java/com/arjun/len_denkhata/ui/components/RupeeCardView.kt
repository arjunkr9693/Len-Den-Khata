package com.arjun.len_denkhata.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RupeeCardView(title: String, value: Double) {
    Card(
        modifier = Modifier // Adjust width as needed
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "%.2f".format(value), // Format to 2 decimal places with rupee symbol
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun RupeeCardRow(totalCredit: Double, totalDebit: Double, todayDue: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        RupeeCardView(title = "Will Give", value = totalCredit)
        RupeeCardView(title = "Will Get", value = totalDebit)
        RupeeCardView(title = "Today Due", value = todayDue)
    }
}

// Example usage:
@Preview
@Composable
fun ExampleUsage() {
    RupeeCardRow(totalCredit = 150000.50, totalDebit = 800.25, todayDue = 250.75)
}