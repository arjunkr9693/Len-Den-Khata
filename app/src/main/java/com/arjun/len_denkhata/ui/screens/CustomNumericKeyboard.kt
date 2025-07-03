package com.arjun.len_denkhata.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun CustomNumericKeyboard(
    onDigitClicked: (String) -> Unit,
    onClearClicked: () -> Unit,
    onBackspaceClicked: () -> Unit,
    onOperatorClick: (String) -> Unit,
    onDecimalClicked: () -> Unit,
    onPercentageClicked: () -> Unit,
    onMemoryPlusClicked: () -> Unit,
    onMemoryMinusClicked: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    // Helper function to add haptic feedback to any action
    fun performHapticAction(action: () -> Unit) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        action()
    }

    // Memoize button colors to avoid recomputation
    val operatorButtonColors =
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )

    val functionButtonColors =
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // First row: C, M+, M-, Backspace
            Row(modifier = Modifier.fillMaxWidth()) {
                KeyboardButton(
                    text = "C",
                    onClick = { performHapticAction(onClearClicked) },
                    modifier = Modifier.weight(1f).padding(2.dp),
                    colors = functionButtonColors
                )
                KeyboardButton(
                    text = "M+",
                    onClick = { performHapticAction(onMemoryPlusClicked) },
                    modifier = Modifier.weight(1f).padding(2.dp),
                    colors = functionButtonColors
                )
                KeyboardButton(
                    text = "M-",
                    onClick = { performHapticAction(onMemoryMinusClicked) },
                    modifier = Modifier.weight(1f).padding(2.dp),
                    colors = functionButtonColors
                )

                // Backspace button with long press handling
                BackspaceButton(
                    onBackspaceClicked = onBackspaceClicked,
                    modifier = Modifier.weight(1f).padding(2.dp),
                    colors = functionButtonColors
                )
            }

            // Second row: 7, 8, 9, ÷
            Row(modifier = Modifier.fillMaxWidth()) {
                KeyboardButton("7", { performHapticAction { onDigitClicked("7") } }, Modifier.weight(1f).padding(2.dp))
                KeyboardButton("8", { performHapticAction { onDigitClicked("8") } }, Modifier.weight(1f).padding(2.dp))
                KeyboardButton("9", { performHapticAction { onDigitClicked("9") } }, Modifier.weight(1f).padding(2.dp))
                KeyboardButton("÷", { performHapticAction { onOperatorClick("÷") } }, Modifier.weight(1f).padding(2.dp), operatorButtonColors)
            }

            // Third row: 4, 5, 6, ×
            Row(modifier = Modifier.fillMaxWidth()) {
                KeyboardButton("4", { performHapticAction { onDigitClicked("4") } }, Modifier.weight(1f).padding(2.dp))
                KeyboardButton("5", { performHapticAction { onDigitClicked("5") } }, Modifier.weight(1f).padding(2.dp))
                KeyboardButton("6", { performHapticAction { onDigitClicked("6") } }, Modifier.weight(1f).padding(2.dp))
                KeyboardButton("×", { performHapticAction { onOperatorClick("×") } }, Modifier.weight(1f).padding(2.dp), operatorButtonColors)
            }

            // Fourth row: 1, 2, 3, -
            Row(modifier = Modifier.fillMaxWidth()) {
                KeyboardButton("1", { performHapticAction { onDigitClicked("1") } }, Modifier.weight(1f).padding(2.dp))
                KeyboardButton("2", { performHapticAction { onDigitClicked("2") } }, Modifier.weight(1f).padding(2.dp))
                KeyboardButton("3", { performHapticAction { onDigitClicked("3") } }, Modifier.weight(1f).padding(2.dp))
                KeyboardButton("-", { performHapticAction { onOperatorClick("-") } }, Modifier.weight(1f).padding(2.dp), operatorButtonColors)
            }

            // Fifth row: 0, ., %, +
            Row(modifier = Modifier.fillMaxWidth()) {
                KeyboardButton("0", { performHapticAction { onDigitClicked("0") } }, Modifier.weight(1.5f).padding(2.dp))
                KeyboardButton(".", { performHapticAction(onDecimalClicked) }, Modifier.weight(0.75f).padding(2.dp))
                KeyboardButton("%", { performHapticAction(onPercentageClicked) }, Modifier.weight(0.75f).padding(2.dp), operatorButtonColors)
                KeyboardButton("+", { performHapticAction { onOperatorClick("+") } }, Modifier.weight(1f).padding(2.dp), operatorButtonColors)
            }
        }
    }
}

/**
 * Reusable keyboard button component to reduce code duplication
 */
@Composable
private fun KeyboardButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: androidx.compose.material3.ButtonColors = ButtonDefaults.buttonColors()
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = colors
    ) {
        Text(text, fontSize = 24.sp)
    }
}

/**
 * Specialized backspace button with long press functionality
 */
@Composable
private fun BackspaceButton(
    onBackspaceClicked: () -> Unit,
    modifier: Modifier = Modifier,
    colors: androidx.compose.material3.ButtonColors = ButtonDefaults.buttonColors()
) {
    val hapticFeedback = LocalHapticFeedback.current
    val backspaceInteractionSource = remember { MutableInteractionSource() }
    val isBackspacePressed by backspaceInteractionSource.collectIsPressedAsState()

    Button(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onBackspaceClicked()
        },
        modifier = modifier,
        interactionSource = backspaceInteractionSource,
        colors = colors
    ) {
        Text("⌫", fontSize = 20.sp)
    }

    // Effect to handle continuous backspace when pressed
    LaunchedEffect(isBackspacePressed) {
        if (isBackspacePressed) {
            // Vibrate immediately when long press starts
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            // Initial delay before continuous deletion starts
            delay(500)
            while (isBackspacePressed) {
                onBackspaceClicked()
                // Delay between each deletion when held
                delay(50)
            }
        }
    }
}

@Preview
@Composable
fun CustomNumericKeyboardPreview() {
    CustomNumericKeyboard(
        onDigitClicked = {},
        onClearClicked = {},
        onBackspaceClicked = {},
        onOperatorClick = {},
        onDecimalClicked = {},
        onPercentageClicked = {},
        onMemoryPlusClicked = {},
        onMemoryMinusClicked = {}
    )
}