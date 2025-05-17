package com.arjun.len_denkhata.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arjun.len_denkhata.R
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun RupeeCardView(title: String, value: Double, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center // Center the title text
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatToRupee(value),
                fontSize = 18.sp,
                textAlign = TextAlign.Center // Center the value text as well
            )
        }
    }
}

fun formatToRupee(value: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    format.maximumFractionDigits = 2
    format.currency = Currency.getInstance("INR")
    return format.format(value)
}

@Composable
fun RupeeCardRow(totalCredit: Double, totalDebit: Double, todayDue: Double, screenWidth: Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp), // Adjust horizontal padding
        horizontalArrangement = Arrangement.SpaceEvenly // Distribute space evenly
    ) {
        val cardWidth = (screenWidth / 3) - 16.dp // Calculate width for each card (accounting for padding)
        RupeeCardView(
            title = stringResource(R.string.will_give),
            value = totalCredit.absoluteValue,
            modifier = Modifier.width(cardWidth)
        )
        RupeeCardView(
            title = stringResource(R.string.will_get),
            value = totalDebit,
            modifier = Modifier.width(cardWidth)
        )
        RupeeCardView(
            title = stringResource(R.string.today_due),
            value = todayDue,
            modifier = Modifier.width(cardWidth)
        )
    }
}

// Example usage:
@Preview(widthDp = 360) // Simulate a typical phone screen width
@Composable
fun ExampleUsage() {
    RupeeCardRow(
        totalCredit = 150000.50,
        totalDebit = 800.25,
        todayDue = 250.75,
        screenWidth = 360.dp // Use a realistic screen width for preview
    )
}

@Preview(widthDp = 360) // Simulate a typical phone screen width with longer text
@Composable
fun ExampleUsageLongText() {
    RupeeCardRow(
        totalCredit = 15000000.50,
        totalDebit = 8000000.25,
        todayDue = 2500000.75,
        screenWidth = 360.dp // Use a realistic screen width for preview
    )
}