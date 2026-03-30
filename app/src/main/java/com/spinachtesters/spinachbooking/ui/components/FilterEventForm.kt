package com.spinachtesters.spinachbooking.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.TextField
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.spinachtesters.spinachbooking.ui.theme.PrimaryGreen
import com.spinachtesters.spinachbooking.ui.theme.SecondaryGreen
import com.spinachtesters.spinachbooking.ui.theme.TextGreen
import com.spinachtesters.spinachbooking.ui.viewmodels.FilterEventViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FilterEventForm(
    viewModel: FilterEventViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            MakeGenericTextfield(
                uiState.title,
                viewModel::onTitleChanged,
                "Event Name",
                "eventTitle"
            )
        }

        item {
            MakeGenericTextfield(
                uiState.minPrice,
                viewModel::onMinPriceChanged,
                "Min Price ($)",
                "minPrice",
                true
            )
        }

        item {
            MakeGenericTextfield(
                uiState.maxPrice,
                viewModel::onMaxPriceChanged,
                "Max Price ($)",
                "maxPrice",
                true
            )
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth(0.9f)
            ){
                DateSelector(
                    "Event Date",
                    "search_date",
                    uiState.date,
                    viewModel::onDateChanged
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    TimeSelector(
                        "Start Time",
                        "search_start_time",
                        uiState.start,
                        viewModel::onStartChanged
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    TimeSelector(
                        "End Time",
                        "search_end_time",
                        uiState.end,
                        viewModel::onEndChanged
                    )
                }
            }
        }

        item {
            MakeGenericTextfield(
                uiState.location,
                viewModel::onLocationChanged,
                "Location",
                "location"
            )
        }

        item {
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                TextField(
                    value = uiState.eventType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Event Type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = TextGreen,
                        placeholderColor = Color(0xCC1F2A1F),
                        cursorColor = PrimaryGreen,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = SecondaryGreen,
                        backgroundColor = SecondaryGreen
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_eventType_input")
                )

                Spacer(modifier = Modifier.height(4.dp))

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("Sports", "Theater", "Concert", "Film").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = TextGreen) },
                            onClick = {
                                viewModel.onEventTypeChanged(option);
                                expanded = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (!uiState.eventType.isEmpty()) {
            when (uiState.eventType) {
                "Theater" -> {
                    item {
                        MakeGenericTextfield(
                            uiState.theaterWriter,
                            viewModel::onTheaterWriterChanged,
                            "Play Writer",
                            "writer"
                        )
                    }
                    item {
                        MakeGenericTextfield(
                            uiState.theaterGenre,
                            viewModel::onTheaterGenreChanged,
                            "Play Genre",
                            "genre"
                        )
                    }
                    item {
                        MakeGenericTextfield(
                            uiState.theaterDuration,
                            viewModel::onTheaterDurationChanged,
                            "Play Duration (minutes)",
                            "duration",
                            true
                        )
                    }
                }
                "Sports" -> {
                    item {
                        MakeGenericTextfield(
                            uiState.sportType,
                            viewModel::onSportTypeChanged,
                            "Sport Type",
                            "sport"
                        )
                    }
                    item {
                        MakeGenericTextfield(
                            uiState.sportHomeTeam,
                            viewModel::onSportHomeTeamChanged,
                            "Home Team",
                            "home team"
                        )
                    }
                    item {
                        MakeGenericTextfield(
                            uiState.sportVisitingTeam,
                            viewModel::onSportVisitingTeamChanged,
                            "Visiting Team",
                            "visiting team"
                        )
                    }
                    item {
                        MakeGenericTextfield(
                            uiState.sportLeague,
                            viewModel::onSportLeagueChanged,
                            "Sport League",
                            "league"
                        )
                    }
                }
                "Film" -> {
                    item {
                        MakeGenericTextfield(
                            uiState.filmDirector,
                            viewModel::onFilmDirectorChanged,
                            "Film Director",
                            "director"
                        )
                    }
                    item {
                        MakeGenericTextfield(
                            uiState.filmRuntime,
                            viewModel::onFilmRuntimeChanged,
                            "Film Runtime (minutes)",
                            "runtime",
                            true
                        )
                    }
                    item {
                        MakeGenericTextfield(
                            uiState.filmRating,
                            viewModel::onFilmRatingChanged,
                            "Film Rating (?/5)",
                            "rating",
                            true
                        )
                    }
                    item {
                        MakeGenericTextfield(
                            uiState.filmGenre,
                            viewModel::onFilmGenreChanged,
                            "Film Genre",
                            "genre"
                        )
                    }
                }
                "Concert" -> {
                    item {
                        MakeGenericTextfield(
                            uiState.concertMainArtist,
                            viewModel::onConcertArtistChanged,
                            "Concert Artist",
                            "artist"
                        )
                    }
                    item {
                        MakeGenericTextfield(
                            uiState.concertGenre,
                            viewModel::onConcertGenreChanged,
                            "Concert Genre",
                            "genre"
                        )
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.isOpenOnly,
                    onCheckedChange = viewModel::onIsOnlyOpenChanged
                )
                Text("Only show open events")
            }
        }
    }
}

@Composable
private fun MakeGenericTextfield(
    value: String,
    onValueChangeCb: (String) -> Unit,
    label: String,
    key: String,
    numOnly: Boolean = false
) {
    return OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (numOnly) {
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                    onValueChangeCb(newValue)
                }
            } else {
                onValueChangeCb(newValue)
            }
        },
        label = { Text(label) },
        placeholder = { Text(text = "Enter the event's $key") },
        singleLine = true,
        keyboardOptions = if (numOnly) {
            KeyboardOptions(keyboardType = KeyboardType.Number)
        } else {
            KeyboardOptions.Default
        },
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .testTag("create_${key}_input"),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = TextGreen,
            placeholderColor = Color(0xCC1F2A1F),
            backgroundColor = SecondaryGreen,
            cursorColor = PrimaryGreen,
            focusedBorderColor = PrimaryGreen,
            unfocusedBorderColor = SecondaryGreen
        )
    )
}
