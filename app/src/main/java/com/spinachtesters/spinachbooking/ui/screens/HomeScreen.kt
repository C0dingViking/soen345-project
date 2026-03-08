package com.spinachtesters.spinachbooking.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spinachtesters.spinachbooking.R
import com.spinachtesters.spinachbooking.domain.models.ConcertDetails
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import com.spinachtesters.spinachbooking.ui.components.cards.BookedCard
import com.spinachtesters.spinachbooking.ui.components.cards.EventCard
import com.spinachtesters.spinachbooking.ui.theme.BackgroundGrey
import com.spinachtesters.spinachbooking.ui.theme.SecondaryGreen
import com.spinachtesters.spinachbooking.ui.theme.TextGreen
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val sampleEvents = listOf(
    Event(
        id = "1",
        title = "Canadiens vs Rangers",
        date = LocalDate.of(2024, 1, 23),
        startTime = LocalDateTime.of(2024, 1, 23, 17, 0),
        endTime = LocalDateTime.of(2024, 1, 23, 19, 0),
        ticketPrice = 120.0,
        location = "Montreal, QC",
        status = "AVAILABLE",
        details = SportDetails()
    ),
    Event(
        id = "2",
        title = "Canadiens vs Rangers",
        date = LocalDate.of(2024, 1, 23),
        startTime = LocalDateTime.of(2024, 1, 23, 20, 0),
        endTime = LocalDateTime.of(2024, 1, 23, 23, 0),
        ticketPrice = 200.0,
        location = "Montreal, QC",
        status = "AVAILABLE",
        details = SportDetails()
    ),
    Event(
        id = "2",
        title = "Canadiens vs Rangers",
        date = LocalDate.of(2024, 1, 23),
        startTime = LocalDateTime.of(2024, 1, 23, 20, 0),
        endTime = LocalDateTime.of(2024, 1, 23, 23, 0),
        ticketPrice = 200.0,
        location = "Montreal, QC",
        status = "AVAILABLE",
        details = SportDetails()
    )
)

val sampleBookings = listOf(
    Event(
        id = "3",
        title = "Drake Concert",
        date = LocalDate.of(2024, 1, 23),
        startTime = LocalDateTime.of(2024, 1, 23, 20, 0),
        endTime = LocalDateTime.of(2024, 1, 23, 23, 0),
        ticketPrice = 200.0,
        location = "Laval, QC",
        status = "BOOKED",
        details = ConcertDetails()
    ),
    Event(
        id = "4",
        title = "Canadiens vs Rangers",
        date = LocalDate.of(2024, 1, 23),
        startTime = LocalDateTime.of(2024, 1, 23, 20, 0),
        endTime = LocalDateTime.of(2024, 1, 23, 23, 0),
        ticketPrice = 200.0,
        location = "Laval, QC",
        status = "BOOKED",
        details = SportDetails()
    )
)

@Composable
@Preview
fun HomeScreen() {
    @Composable
    fun HeaderSection() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {

            Image(
                painter = painterResource(R.drawable.party),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Text(
                text = "Search and filter events by date, location, and category:",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }

    @Composable
    fun SearchBar() {

        var text by remember { mutableStateOf("") }

        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Search for an event...", color = TextGreen) },
            shape = RoundedCornerShape(50),
            trailingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BackgroundGrey,
                unfocusedContainerColor = SecondaryGreen
            )
        )
    }

    @Composable
    fun AvailableEventsSection(events: List<Event>) {

        Column {
            Text(
                text = "Available events:",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events) { event ->
                    EventCard(
                        title = event.title,
                        date = formatDate(event.date),
                        location = event.location,
                        onClick = {}
                    )
                }
            }
        }
    }

    @Composable
    fun BookedEventsSection(events: List<Event>) {

        Column {

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Booked events:",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    "Alerts",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {

                events.forEachIndexed { index, event ->
                    BookedCard(
                        title = event.title,
                        location = event.location,
                        date = formatDate(event.date),
                        time = formatTime(event.startTime),
                        checked = index == 0,
                        onCheckedChange = {}
                    )
                }
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {

        HeaderSection()

        Column(modifier = Modifier.padding(10.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            SearchBar()

            Spacer(modifier = Modifier.height(16.dp))

            AvailableEventsSection(sampleEvents)

            Spacer(modifier = Modifier.height(16.dp))

            BookedEventsSection(sampleBookings)
        }
    }

}

fun formatDate(dateTime: LocalDate): String {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")
    return dateTime.format(dateFormatter)
}

fun formatTime(dateTime: LocalDateTime): String {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    return dateTime.format(timeFormatter)
}



