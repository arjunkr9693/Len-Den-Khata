package com.arjun.len_denkhata.ui.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.arjun.len_denkhata.R
import com.arjun.len_denkhata.data.database.customer.CustomerEntity
import kotlin.math.absoluteValue

@Composable
fun CustomerListItem(
    customer: CustomerEntity,
    onClick: (CustomerEntity) -> Unit,
    getLastUpdatedMessage: (Long) -> String
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Log.d("CustomerListItem", "Customer: ${customer.profilePictureUri}")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onClick(customer) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile picture
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(48.dp)
                        .clip(CircleShape) // Clip the Box to CircleShape
                ) {
                    if (customer.profilePictureUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(customer.profilePictureUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile picture",
                            modifier = Modifier
//                                .fillMaxSize() // Fill the clipped Box
                                .clip(CircleShape), // Ensure image is clipped
                            contentScale = ContentScale.Crop, // Try ContentScale.Fit if distortion persists
                            error = painterResource(id = R.drawable.ic_person),
                            placeholder = painterResource(id = R.drawable.ic_person)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_person),
                            contentDescription = "Default profile",
                            modifier = Modifier
//                                .fillMaxSize() // Fill the clipped Box
                                .clip(CircleShape), // Ensure image is clipped
                            contentScale = ContentScale.Crop // Try ContentScale.Fit if distortion persists
                        )
                    }
                }

                // Customer name and last updated info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    val displayName = if (customer.name.length > 15) {
                        customer.name.take(15) + "..."
                    } else {
                        customer.name.ifEmpty { customer.phone ?: "Unknown" }
                    }

                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        color = colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = getLastUpdatedMessage(customer.lastUpdated),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }

            // Balance amount
            val amountColor = if (customer.overallBalance >= 0) Color.Red else Color.Green
            Text(
                text = "₹${"%.2f".format(customer.overallBalance.absoluteValue)}",
                style = MaterialTheme.typography.bodyLarge,
                color = amountColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}