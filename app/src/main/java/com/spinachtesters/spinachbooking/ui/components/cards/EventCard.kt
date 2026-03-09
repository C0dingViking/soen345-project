package com.spinachtesters.spinachbooking.ui.components.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spinachtesters.spinachbooking.R
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import com.spinachtesters.spinachbooking.ui.theme.BackgroundGrey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Preview
@Composable
private fun EventCardPreview() {
    EventCard(
        event = Event(
            id = "preview",
            title = "Canadiens vs. Rangers",
            date = LocalDate.of(2024, 1, 23),
            startTime = LocalDateTime.of(2024, 1, 23, 17, 0),
            endTime = LocalDateTime.of(2024, 1, 23, 19, 0),
            ticketPrice = 120.0,
            location = "Montreal, QC",
            status = "AVAILABLE",
            details = SportDetails()
        ),
        onClick = {}
    )
}

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit
) {
    val imageRes = remember(event.details.detailType) {
        resolveEventImageRes(event.details.detailType)
    }
    val formattedDate = remember(event.date) {
        event.date.format(DateTimeFormatter.ofPattern("MMM dd"))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = BackgroundGrey),
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = event.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    fontWeight = FontWeight.Medium
                )

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = event.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Right
                    )
                    Text(
                        text = event.location,
                        fontSize = 8.sp,
                        textAlign = TextAlign.Right
                    )
                }
            }
        }
    }
}

private fun resolveEventImageRes(detailType: String): Int {
    return when (detailType.lowercase()) {
        "sport" -> R.drawable.sport
        "theater" -> R.drawable.theater
        "concert" -> R.drawable.concert
        "film", "cinema" -> R.drawable.cinema
        else -> R.drawable.placeholder
    }
}
