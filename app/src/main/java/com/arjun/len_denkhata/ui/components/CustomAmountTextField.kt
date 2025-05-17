package com.arjun.len_denkhata.ui.components

import android.view.Gravity
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CustomAmountTextField(
    value: String,
    modifier: Modifier = Modifier,
    onFocusChanged: (Boolean) -> Unit
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .height(56.dp) // Matches OutlinedTextField height
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.small
            ),
        factory = { context ->
            AppCompatEditText(context).apply {
                isFocusable = true
                isFocusableInTouchMode = true
                showSoftInputOnFocus = false
                background = null
                hint = "Amount" // Hint added
                gravity = Gravity.CENTER_VERTICAL // Center text vertically
                setPadding(32, 0, 32, 0)
                textSize = 16f

                setOnFocusChangeListener { _, hasFocus ->
                    onFocusChanged(hasFocus)
                }
            }
        },
        update = { view: EditText ->
            if (view.text.toString() != value) {
                view.setText(value)
            }
            view.setSelection(value.length)
        }
    )
}
