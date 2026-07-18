package com.zenodotus.ringer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DateIcon(displayDate: String) {
    val parts = displayDate.split(" ") // bv ["16", "Apr", "2026"]

    Column(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White)
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp) // 👈 kleiner maken
                .background(Color.Red),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = parts[1],
                color = Color.White,
                fontSize = 7.sp, // 👈 kleiner
                modifier = Modifier.offset(y = (-3).dp)
            )
        }

        Text(
            text = parts[0],
            fontSize = 8.sp, // 👈 kleiner maar nog leesbaar
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(y = (-3).dp)
        )
    }
}

@Composable
fun IconSlot(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(32.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}