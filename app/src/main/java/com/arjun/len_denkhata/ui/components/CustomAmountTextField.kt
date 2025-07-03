package com.arjun.len_denkhata.ui.components

import android.widget.EditText
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.appcompat.widget.AppCompatEditText
import android.view.Gravity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color // Import Color

@Composable
fun CustomAmountTextField(
    value: String,
    modifier: Modifier = Modifier,
    onFocusChanged: (Boolean) -> Unit,
    shouldClearFocus: Boolean = false, // Add this parameter
    onFocusCleared: () -> Unit = {} // Add this callback
) {
    val editTextRef = remember { mutableStateOf<EditText?>(null) }

    // Get current colors from MaterialTheme
    val textColor = MaterialTheme.colorScheme.onSurface
    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant // A common choice for hints

    // Clear focus when requested
    LaunchedEffect(shouldClearFocus) {
        if (shouldClearFocus) {
            editTextRef.value?.clearFocus()
            onFocusCleared()
        }
    }

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
                hint = "Amount"
                gravity = Gravity.CENTER_VERTICAL
                setPadding(32, 0, 32, 0)
                textSize = 16f

                // --- START OF CHANGES ---
                // Set text color from MaterialTheme
                setTextColor(textColor.toArgb())
                // Set hint color from MaterialTheme
                setHintTextColor(hintColor.toArgb())
                // --- END OF CHANGES ---

                setOnFocusChangeListener { _, hasFocus ->
                    onFocusChanged(hasFocus)
                }

                editTextRef.value = this
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