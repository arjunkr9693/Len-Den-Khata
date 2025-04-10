package com.arjun.len_denkhata.data.utils

import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionEntity

import com.google.gson.Gson

fun MonthBookTransactionEntity.toJson(): String {
    return Gson().toJson(this)
}

fun String.toMonthBookTransaction(): MonthBookTransactionEntity? {
    return try {
        Gson().fromJson(this, MonthBookTransactionEntity::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}