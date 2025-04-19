package com.arjun.len_denkhata.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.arjun.len_denkhata.R

@Composable
fun CustomAmountTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    showCursor: Boolean,
    cursorColor: Color
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        cursorBrush = SolidColor(if (showCursor) cursorColor else Color.Transparent),
        textStyle = LocalTextStyle.current.copy(
            color = LocalContentColor.current
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.None
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.extraSmall)
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        MaterialTheme.shapes.extraSmall
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (value.text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.amount),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}