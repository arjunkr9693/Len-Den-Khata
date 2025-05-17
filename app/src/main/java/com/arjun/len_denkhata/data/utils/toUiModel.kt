package com.arjun.len_denkhata.data.utils

import com.arjun.len_denkhata.data.database.TransactionUiModel
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import java.util.Date

fun CustomerTransactionEntity.toUiModel(): TransactionUiModel {

    return TransactionUiModel(
        id = this.id,
        amount = this.amount,
        description = this.description,
        formattedTimestamp = DateFormatters.timestampFormatter.format(Date(this.timestamp)),
        formattedEditedOn = if (isEdited && editedOn != null) {
            DateFormatters.timestampFormatter.format(Date(editedOn))
        } else null,
        isCredit = this.isCredit,
        isMadeByOwner = this.isMadeByOwner,
        isEdited = this.isEdited
    )
}
