package com.spinachtesters.spinachbooking.ui.components

import android.app.DatePickerDialog
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
fun DateSelector(
    label: String = "",
    selectedDate: String = "",
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePicker = DatePickerDialog(
        context,
        { _, y, m, d ->
            onDateSelected("$y-${m + 1}-$d")
        },
        year, month, day
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { datePicker.show() }
    ) {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = onDateSelected,
            readOnly = true,
            label = { Text(label) },
            singleLine = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth().testTag("create_dateSelect_input"),
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
