package com.arjun.len_denkhata.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arjun.len_denkhata.data.database.customer.CustomerEntity
import kotlin.math.absoluteValue

@Composable
fun CustomerListItem(customer: CustomerEntity, onClick: (CustomerEntity) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme // Get the current color scheme
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(colorScheme.surface), // Transparent at top, bottomColor at bottom
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onClick(customer) },
//            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
        elevation = CardDefaults.cardElevation(4.dp) // Disable default shadow
    ) {
            Row(
                modifier = Modifier
                    .padding(16.dp) // Padding inside the card
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayName = if (customer.name.length > 15) {
                    customer.name.take(15) + "..."
                } else {
                    customer.name.ifEmpty { customer.phone }
                }

                Text(
                    text = displayName,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

                val amountColor = if (customer.overallBalance >= 0) Color.Red else Color.Green

                Text(
                    text = "₹${"%.2f".format(customer.overallBalance.absoluteValue)}",
                    color = amountColor
                )
            }

    }
}

//// Custom Modifier for shadow with opacity gradient
//fun Modifier.shadowWithOpacityGradient(bottomColor: Color): Modifier = composed {
//
//
//    this.then(
//        Modifier
//            .shadow(
//                elevation = 8.dp, // Use Float for elevation
////                shape = RoundedCornerShape(4.dp), // Adjust corner radius as needed
////                clip = true
//            )
//            .background(gradientBrush, shape = RoundedCornerShape(4.dp)) // Apply gradient background
//    )
//}