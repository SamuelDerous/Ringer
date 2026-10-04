package com.zenodotus.ringer

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.Instant
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun DatePickerField(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit
) {
    var dateText by remember {
        mutableStateOf(date.format(DateTimeFormatter.ofPattern("EEEE dd MMM yyyy")))
    }
    var showDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = dateText,
        onValueChange = { },
        label = { Text("Vanaf") },
        readOnly = true,
        modifier = Modifier
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    showDialog = true
                    focusManager.clearFocus()
                }
            }
    )

    if (showDialog) {
        DatePickerModal(
            onConfirm = { newDate ->
                dateText = newDate.format(
                    DateTimeFormatter.ofPattern("EEEE dd MMM yyyy")
                )
                onDateChange(newDate)
                showDialog = false
            },
            onDismiss = {
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        onConfirm(selectedDate)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuleer")
            }
        }
    ) {
        DatePicker(
            state = datePickerState
        )
    }
}