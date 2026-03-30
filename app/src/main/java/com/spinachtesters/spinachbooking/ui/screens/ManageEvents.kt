package com.spinachtesters.spinachbooking.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.ui.components.MinimalTopBar
import com.spinachtesters.spinachbooking.ui.components.cards.EditableEventCard
import com.spinachtesters.spinachbooking.ui.theme.Background
import com.spinachtesters.spinachbooking.ui.theme.PoppinsFontFamily
import com.spinachtesters.spinachbooking.ui.viewmodels.ManageEventsViewModel

@Composable
@Preview
fun ManageEventsScreenPreview() {
    ManageEventsScreen(navController = rememberNavController())
}

@Composable
fun ManageEventsScreen(
    navController: NavController,
    viewModel: ManageEventsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var eventToDelete by remember { mutableStateOf<Event?>(null) }

    // automatically reloads the events whenever navigating to this page (even if not recomposed)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        viewModel.loadEvents()
    }

    Scaffold(
        topBar = { MinimalTopBar(false) },
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
                        text = "Manage Events",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 20.sp,
                        color = Color.White,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.80f),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.events.isNullOrEmpty()) {
                        Text(
                            text = "No events have been created yet... make one below!",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 16.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .fillMaxWidth(0.90f)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("manage_events_list"),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.events!!) { event ->
                                EditableEventCard(
                                    subject = event,
                                    clickDeleteCallback = { eventToDelete = event }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { navController.navigate("add_event") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC857)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.50f)
                            .height(65.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Event",
                            tint = Color.Black,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
            if (eventToDelete != null) {
                AlertDialog(
                    onDismissRequest = { eventToDelete = null },
                    title = { Text("Delete Event") },
                    text = { Text("Are you sure you want to delete this event?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteEvent(eventToDelete!!)
                                eventToDelete = null
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { eventToDelete = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

        }
    )
}
