package com.arjun.len_denkhata.data.utils

import java.text.SimpleDateFormat
import java.util.Locale

object DateFormatters {
    val timestampFormatter by lazy {
        SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault())
    }

    val editedFormatter by lazy {
        SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
    }

    val dateGroupFormat by lazy {
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    }
    val fullTimestampFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault())
}
