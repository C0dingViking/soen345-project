package com.spinachtesters.spinachbooking.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.spinachtesters.spinachbooking.ui.theme.SecondaryGreen
import com.spinachtesters.spinachbooking.ui.theme.TextGreen
import java.util.Calendar

@Composable
fun TimeSelector(
    label: String,
    key: String,
    selectedTime: String,
    onTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current

    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val timePicker = TimePickerDialog(
        context,
        { _, h, m ->
            val formatted = String.format("%02d:%02d", h, m)
            onTimeSelected(formatted)
        },
        hour, minute, true
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { timePicker.show() }
    ) {
        OutlinedTextField(
            value = selectedTime,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("Select time") },
            singleLine = true,
            enabled = false,
            modifier = Modifier
                .fillMaxWidth().testTag("create_${key}_input"),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                disabledTextColor = TextGreen,
                disabledPlaceholderColor = Color(0xCC1F2A1F),
                disabledBorderColor = SecondaryGreen,
                disabledLabelColor = TextGreen,
                disabledTrailingIconColor = TextGreen,
                backgroundColor = SecondaryGreen
            )
        )
    }
}
