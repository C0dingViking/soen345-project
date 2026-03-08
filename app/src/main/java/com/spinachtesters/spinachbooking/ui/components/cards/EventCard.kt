package com.spinachtesters.spinachbooking.ui.components.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.spinachtesters.spinachbooking.R
import com.spinachtesters.spinachbooking.ui.theme.*

@Preview
@Composable
private fun EventCardPreview() {
    EventCard(
        title = "Canadiens vs. Rangers",
        date = "Jan 23",
        location = "Montreal, QC",
        onClick = {}
    )
}

@Composable
fun EventCard(
    imageRes: Int = R.drawable.placeholder,
    title: String,
    date: String,
    location: String,
    onClick: () -> Unit
) {

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(180.dp)
            .padding(8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = BackgroundGrey)
    ) {

        Column(
            modifier = Modifier.padding(10.dp)
        ) {

            Image(
                painter = painterResource(imageRes),
                contentDescription = title,
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
                // Date on the left
                Text(
                    text = date,
                    fontWeight = FontWeight.Bold
                )

                // Title & Location on the right
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Right
                    )
                    Text(
                        text = location,
                        fontSize = 8.sp,
                        textAlign = TextAlign.Right
                    )
                }
            }
        }
    }
}
