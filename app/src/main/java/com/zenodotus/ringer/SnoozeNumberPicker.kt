package com.zenodotus.ringer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SnoozeNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int = 1,
    max: Int = 60,
    step: Int = 5,
    cap: String,
    enabled: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.width(80.dp)
    ) {
        IconButton(
            onClick = {
                if (value - step >= min) onValueChange(value - step)
            },
            modifier = Modifier.size(32.dp),
            enabled = enabled
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minder")
        }

        Text(
            text = "$value $cap",
            color = if (enabled) LocalContentColor.current else Color.Gray
        )

        IconButton(
            onClick = {
                if (value + step <= max) onValueChange(value + step)
            },
            modifier = Modifier.size(32.dp),
            enabled = enabled
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Meer")
        }
    }
}