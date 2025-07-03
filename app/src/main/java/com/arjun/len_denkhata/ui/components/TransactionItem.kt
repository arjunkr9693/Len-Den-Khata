package com.arjun.len_denkhata.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arjun.len_denkhata.R
import com.arjun.len_denkhata.data.database.TransactionUiModel

@Composable
fun TransactionItem(
    transaction: TransactionUiModel,
    onDelete: (transactionId: Long) -> Unit,
    onEdit: (transactionId: Long, isCredit: Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) } // State to manage dropdown menu visibility

    // Determine if the transaction is from the owner or another user
    val isMadeByOwner = transaction.isMadeByOwner

    // Determine if the transaction is credit or debit based on the owner's perspective
    val isCredit = transaction.isCredit

    // Background color based on debit/credit
    val backgroundColor =
        if (isCredit) colorResource(R.color.creditContainerColor) else colorResource(R.color.debitContainerColor) // Light green for credit, light red for debit

    // Text for "You gave" or "You got"
    val transactionTypeText =
        if (isCredit) stringResource(R.string.you_got) else stringResource(R.string.you_gave)

    // Alignment based on owner or other user
    val alignment = if (isMadeByOwner) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        Column (
            modifier = Modifier
                .widthIn(max = 350.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .clickable { }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                Text(
                    text = transaction.formattedTimestamp,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                if (isMadeByOwner) {
                    Box {
                        IconButton(
                            onClick = { expanded = true },
                            modifier = Modifier
                                .height(20.dp)
                                .width(20.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    onEdit(transaction.id, transaction.isCredit)
                                    expanded = false
                                },
                                text = { Text(stringResource(R.string.edit)) }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    onDelete(transaction.id)
                                    expanded = false
                                },
                                text = { Text(stringResource(R.string.delete)) }
                            )
                        }
                    }
                }

            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = transaction.description ?: "",
                    fontSize = 14.sp,
                    modifier = Modifier.weight(0.6f),
                    textAlign = TextAlign.Start
                )
                // Amount
                Text(
                    text = "₹${"%.2f".format(transaction.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.4f)
                )

            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (transaction.isEdited) {
                        stringResource(R.string.edited_on) + transaction.formattedEditedOn
                    } else {
                        ""
                    },
                    fontSize = 8.sp,
                    color = Color.Gray
                )
                // "You gave" or "You got" text
                Text(
                    text = transactionTypeText,
                    fontSize = 8.sp, // Increased font size for better visibility
                    color = Color.Gray,
                )
            }
        }
    }
}
