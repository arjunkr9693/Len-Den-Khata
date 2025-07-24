package com.arjun.len_denkhata.ui.components

import android.annotation.SuppressLint
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
    shouldClearFocus: Boolean = false,
    onFocusCleared: () -> Unit = {}
) {
    val editTextRef = remember { mutableStateOf<EditText?>(null) }
    val view = LocalView.current

    // Get current colors from MaterialTheme
    val textColor = MaterialTheme.colorScheme.onSurface
    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant

    // Clear focus when requested
    LaunchedEffect(shouldClearFocus) {
        if (shouldClearFocus) {
            editTextRef.value?.let { editText ->
                editText.clearFocus()
                // Also force hide system keyboard using InputMethodManager
                val imm = view.context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                        as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(editText.windowToken, 0)
            }
            onFocusCleared()
        }
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .height(56.dp)
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

                // Set text color from MaterialTheme
                setTextColor(textColor.toArgb())
                // Set hint color from MaterialTheme
                setHintTextColor(hintColor.toArgb())

                setOnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        // When gaining focus, ensure system keyboard is hidden
                        val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                                as android.view.inputmethod.InputMethodManager
                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                    }

                    // Use post to avoid timing issues
                    post {
                        onFocusChanged(hasFocus)
                    }
                }

                // Override onTouchEvent to handle taps properly
                @SuppressLint("ClickableViewAccessibility")
                setOnTouchListener { v, event ->
                    when (event.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            if (!hasFocus()) {
                                requestFocus()
                                // Force hide system keyboard immediately
                                val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                                        as android.view.inputmethod.InputMethodManager
                                imm.hideSoftInputFromWindow(windowToken, 0)
                            }
                            true
                        }
                        else -> false
                    }
                }

                editTextRef.value = this
            }
        },
        update = { view: EditText ->
            if (view.text.toString() != value) {
                view.setText(value)
                view.setSelection(value.length)
            }
        }
    )
}