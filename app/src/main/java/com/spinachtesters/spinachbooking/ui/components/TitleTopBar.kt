package com.spinachtesters.spinachbooking.ui.components

import com.spinachtesters.spinachbooking.R

import androidx.compose.runtime.Composable
import com.spinachtesters.spinachbooking.ui.theme.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*

@OptIn(ExperimentalMaterial3Api::class)
@Preview(widthDp = 360, heightDp = 120)
@Composable
fun TitleTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 26.dp)
            ) {

                Text(
                    "Spinach Booking",
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = PrimaryGreen
                )

                Spacer(modifier = Modifier.width(8.dp))

                Image(
                    painter = painterResource(id = R.drawable.spinach_booking_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .width(80.dp)
                        .height(48.dp)
                        .alpha(1f)
                )
            }
        },
    )
}
