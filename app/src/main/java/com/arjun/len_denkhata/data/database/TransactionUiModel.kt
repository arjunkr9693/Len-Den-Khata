package com.arjun.len_denkhata.data.database

import androidx.compose.runtime.Immutable

@Immutable
data class TransactionUiModel(
    val id: Long,
    val amount: Double,
    val description: String?,
    val formattedTimestamp: String,
    val formattedEditedOn: String?, // null if not edited
    val isCredit: Boolean,
    val isMadeByOwner: Boolean,
    val isEdited: Boolean
)
