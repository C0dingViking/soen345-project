package com.spinachtesters.spinachbooking.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.FilmDetails
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import com.spinachtesters.spinachbooking.domain.models.TheaterDetails
import com.spinachtesters.spinachbooking.ui.components.MinimalTopBar
import com.spinachtesters.spinachbooking.ui.theme.Background
import com.spinachtesters.spinachbooking.ui.theme.ButtonYellow
import com.spinachtesters.spinachbooking.ui.theme.PoppinsFontFamily
import com.spinachtesters.spinachbooking.ui.theme.TextGreen
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Preview
@Composable
private fun EventDetailScreenPreview() {
    EventDetailScreen(
        eventId = "01",
        navController = rememberNavController()
    )
}

@Composable
fun EventDetailScreen(
    eventId: String?,
    navController: NavController,
) {
    var event = Event(
        id = "1",
        title = "Canadiens vs. Rangers",
        date = LocalDate.of(2026, 12, 14),
        startTime = LocalDateTime.of(2026, 12, 14, 17, 0),
        endTime = LocalDateTime.of(2026, 12, 14, 19, 0),
        ticketPrice = 79.99,
        location = "Montreal, QC",
        status = "BOOKED",
        details = SportDetails(
            homeTeam = "Montreal Canadiens",
            visitingTeam = "New York Rangers"
        )
    )

    var alertsEnabled by remember(event.status) { mutableStateOf(event.status == "BOOKED") }

    Scaffold(
        topBar = {
            MinimalTopBar(
                showBackButton = true,
                goBackCallback = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
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
                    text = formatEventType(event.details.detailType),
                    fontFamily = PoppinsFontFamily,
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.title,
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextGreen
            )

            Spacer(modifier = Modifier.height(20.dp))

            EventMetaRow(label = "Location", value = event.location)
            EventMetaRow(label = "Price", value = "$${"%.2f".format(event.ticketPrice)}")
            EventMetaRow(
                label = "Date",
                value = event.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            )
            EventMetaRow(
                label = "Time",
                value = event.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Alerts", color = TextGreen)
                Checkbox(
                    checked = alertsEnabled,
                    onCheckedChange = { alertsEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Description",
                fontWeight = FontWeight.Medium,
                color = TextGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = buildDescription(event),
                color = TextGreen
            )

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = ButtonYellow)
            ) {
                Text(text = "Cancel", color = TextGreen, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun EventMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextGreen, fontSize = 28.sp)
        Text(text = value, color = TextGreen, fontSize = 28.sp)
    }
}

private fun formatEventType(detailType: String): String {
    return detailType.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

private fun buildDescription(event: Event): String {
    return when (val details = event.details) {
        is SportDetails -> "The matchup between ${details.homeTeam.ifBlank { "the home team" }} and ${details.visitingTeam.ifBlank { "the visiting team" }} is set for an electric game."
        is ConcertDetails -> "Catch ${details.mainArtist.ifBlank { "the featured artist" }} live in this ${details.genre.ifBlank { "music" }} concert event."
        is TheaterDetails -> "Experience a ${details.genre.ifBlank { "theater" }} play by ${details.writer.ifBlank { "a featured writer" }}."
        is FilmDetails -> "Watch a ${details.genre.ifBlank { "feature film" }} directed by ${details.director.ifBlank { "a featured director" }}."
        else -> "Enjoy this event in ${event.location}."
    }
}
