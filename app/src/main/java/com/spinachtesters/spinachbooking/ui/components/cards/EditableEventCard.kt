package com.spinachtesters.spinachbooking.ui.components.cards


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spinachtesters.spinachbooking.domain.models.Event
import com.spinachtesters.spinachbooking.domain.models.SportDetails
import com.spinachtesters.spinachbooking.ui.theme.BackgroundGrey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
@Preview
fun EditableEventCardPreview() {
    EditableEventCard(
        subject = Event(
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
        clickDeleteCallback = {}
    )
}

@Composable
fun EditableEventCard(
    subject: Event,
    clickDeleteCallback: () -> Unit
) {
    val title = subject.title;
    val location = subject.location;
    val date = subject.date.format(DateTimeFormatter.ofPattern("MMM dd"));
    val time = subject.startTime.format(DateTimeFormatter.ofPattern("HH:mm"));

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundGrey)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                val displayTitle = if (title.length > 15) title.take(15) + "..." else title

                Text(
                    text = displayTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = location)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = date,
                        fontWeight = FontWeight.Medium,
                        fontSize = 22.sp
                    )
                    Text(text = time, textAlign = TextAlign.Center)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("manage_delete_${subject.title}"),
                    shape = CircleShape,
                    color = Color(0xFF1F2A1F),
                    onClick = { clickDeleteCallback() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
