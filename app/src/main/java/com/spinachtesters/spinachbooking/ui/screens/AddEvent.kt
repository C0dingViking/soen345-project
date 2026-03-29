package com.spinachtesters.spinachbooking.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.spinachtesters.spinachbooking.ui.components.DateSelector
import com.spinachtesters.spinachbooking.ui.components.MinimalTopBar
import com.spinachtesters.spinachbooking.ui.components.TimeSelector
import com.spinachtesters.spinachbooking.ui.theme.Background
import com.spinachtesters.spinachbooking.ui.theme.PoppinsFontFamily
import com.spinachtesters.spinachbooking.ui.theme.PrimaryGreen
import com.spinachtesters.spinachbooking.ui.theme.SecondaryGreen
import com.spinachtesters.spinachbooking.ui.theme.TextGreen
import com.spinachtesters.spinachbooking.ui.viewmodels.AddEventViewModel

@Composable
@Preview
fun AddEventScreenPreview() {
    AddEventScreen(navController = rememberNavController())
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddEventScreen(
    navController: NavController,
    viewModel: AddEventViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.consumeSuccess()
            navController.navigate("manage_events") {
                popUpTo("manage_events") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = { MinimalTopBar(true){ navController.popBackStack() } },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .background(Background)
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add New Event",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 20.sp,
                        color = Color.White,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = 8.dp,
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(8.dp).testTag("form"),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            MakeGenericTextfield(
                                uiState.eventName,
                                viewModel::onEventNameChanged,
                                "Event Name",
                                "name"
                            )
                        }
                        item {
                            MakeGenericTextfield(
                                uiState.ticketPrice,
                                viewModel::onTicketPriceChanged,
                                "Ticket Price ($)",
                                "price",
                                true
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(0.dp))
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
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                DateSelector(
                                    "Event Date",
                                    "date",
                                    uiState.date,
                                    viewModel::onEventDateChanged
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
                                        "Time Start",
                                        "timeStart",
                                        uiState.timeStart,
                                        viewModel::onTimeStartChanged
                                    )
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    TimeSelector(
                                        "Time End",
                                        "timeEnd",
                                        uiState.timeEnd,
                                        viewModel::onTimeEndChanged
                                    )
                                }
                            }
                        }
                        item {
                            MakeGenericTextfield(
                                uiState.location,
                                viewModel::onEventLocationChanged,
                                "Event Location",
                                "location"
                            )
                        }
                        if (!uiState.eventType.isEmpty()) {
                            if (uiState.eventType == "Theater") {
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
                            }
                            else if (uiState.eventType == "Sports") {
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
                            else if (uiState.eventType == "Film") {
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
                            else if (uiState.eventType == "Concert") {
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
                }

                Spacer(modifier = Modifier.height(if (uiState.errorMessage != null) 8.dp else 24.dp))

                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color(0xFFB00020),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth(0.90f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { viewModel.addEvent() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC857)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.50f)
                            .height(65.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Add Event",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x88000000)), // translucent overlay
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 6.dp
                    )
                }
            }
        }
    )
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
