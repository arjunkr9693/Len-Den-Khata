package com.arjun.len_denkhata.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color

@Composable
fun CustomNumericKeyboard(
    onDigitClicked: (String) -> Unit,
    onClearClicked: () -> Unit,
    onBackspaceClicked: () -> Unit,
    onDivideClicked: () -> Unit,
    onMultiplyClicked: () -> Unit,
    onMinusClicked: () -> Unit,
    onPlusClicked: () -> Unit,
    onDecimalClicked: () -> Unit,
    onPercentageClicked: () -> Unit,
    onEqualsClicked: () -> Unit,
    onMemoryPlusClicked: () -> Unit,
    onMemoryMinusClicked: () -> Unit
) {
    Surface(
        color = Color.White, // Background color for the keyboard
        modifier = Modifier.padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onClearClicked() },
                    modifier = Modifier.weight(1f).padding(2.dp),
                ) {
                    Text("C", fontSize = 20.sp)
                }
                Button(
                    onClick = { onMemoryPlusClicked() },
                    modifier = Modifier.weight(1f).padding(2.dp),
                ) {
                    Text("M+", fontSize = 20.sp)
                }
                Button(
                    onClick = { onMemoryMinusClicked() },
                    modifier = Modifier.weight(1f).padding(2.dp),
                ) {
                    Text("M-", fontSize = 20.sp)
                }
                Button(
                    onClick = { onBackspaceClicked() },
                    modifier = Modifier.weight(1f).padding(2.dp),
                ) {
                    Text("⌫", fontSize = 20.sp)
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onDigitClicked("7") },
                    modifier = Modifier.weight(1f).padding(2.dp),
                ) {
                    Text("7", fontSize = 24.sp)
                }
                Button(
                    onClick = { onDigitClicked("8") },
                    modifier = Modifier.weight(1f).padding(2.dp),
                ) {
                    Text("8", fontSize = 24.sp)
                }
                Button(
                    onClick = { onDigitClicked("9") },
                    modifier = Modifier.weight(1f).padding(2.dp),
                ) {
                    Text("9", fontSize = 24.sp)
                }
                Button(
                    onClick = { onDivideClicked() },
                    modifier = Modifier.weight(1f).padding(2.dp),
                ) {
                    Text("÷", fontSize = 24.sp)
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onDigitClicked("4") },
                    modifier = Modifier.weight(1f).padding(2.dp),
                ) {
                    Text("4", fontSize = 24.sp)
                }
                Button(
                    onClick = { onDigitClicked("5") },
                    modifier = Modifier.weight(1f).padding(2.dp),
                ) {
                    Text("5", fontSize = 24.sp)
                }
                Button(
                    onClick = { onDigitClicked("6") },
                    modifier = Modifier.weight(1f).padding(2.dp),
                ) {
                    Text("6", fontSize = 24.sp)
                }
                Button(
                    onClick = { onMultiplyClicked() },
                    modifier = Modifier.weight(1f).padding(2.dp),
                ) {
                    Text("×", fontSize = 24.sp)
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onDigitClicked("1") },
                    modifier = Modifier.weight(1f).padding(2.dp),
                ) {
                    Text("1", fontSize = 24.sp)
                }
                Button(
                    onClick = { onDigitClicked("2") },
                    modifier = Modifier.weight(1f).padding(2.dp),
                ) {
                    Text("2", fontSize = 24.sp)
                }
                Button(
                    onClick = { onDigitClicked("3") },
                    modifier = Modifier.weight(1f).padding(2.dp),
                ) {
                    Text("3", fontSize = 24.sp)
                }
                Button(
                    onClick = { onMinusClicked() },
                    modifier = Modifier.weight(1f).padding(2.dp),
                ) {
                    Text("-", fontSize = 24.sp)
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onDigitClicked("0") },
                    modifier = Modifier.weight(1.5f).padding(2.dp), // Reduced weight for smaller zero
                ) {
                    Text("0", fontSize = 24.sp)
                }
                Button(
                    onClick = { onDecimalClicked() },
                    modifier = Modifier.weight(0.75f).padding(2.dp), // Adjusted weight
                ) {
                    Text(".", fontSize = 24.sp)
                }
                Button(
                    onClick = { onPercentageClicked() },
                    modifier = Modifier.weight(0.75f).padding(2.dp), // Added percentage button
                ) {
                    Text("%", fontSize = 24.sp)
                }
                Button(
                    onClick = { onPlusClicked() }, // Reversed order: Plus is now here
                    modifier = Modifier.weight(1f).padding(2.dp),
                ) {
                    Text("+", fontSize = 24.sp)
                }
            }
        }
    }
}