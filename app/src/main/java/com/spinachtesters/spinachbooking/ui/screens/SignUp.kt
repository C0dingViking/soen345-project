package com.spinachtesters.spinachbooking.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.OutlinedTextField
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spinachtesters.spinachbooking.ui.components.TitleTopBar
import com.spinachtesters.spinachbooking.ui.theme.*
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun SignUpScreen() {
    Scaffold(
        topBar = {TitleTopBar()},
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Sign up",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp,
                    color = TextGreen,
                    modifier = Modifier
                        .padding(vertical = 24.dp, )
                )
                var selectedOption by remember { mutableStateOf("email") }
                Row(
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .background(SecondaryGreen, RoundedCornerShape(50))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selectedOption == "email") PrimaryGreen else Color.Transparent)
                            .clickable { selectedOption = "email" }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "email",
                            color = if (selectedOption == "email") Color.White else TextGreen
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selectedOption == "phone") PrimaryGreen else Color.Transparent)
                            .clickable { selectedOption = "phone" }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "phone",
                            color = if (selectedOption == "phone") Color.White else TextGreen
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {

                    var userContact by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = userContact,
                        onValueChange = { newText -> userContact = newText },
                        label = {
                            Text(
                                if (selectedOption == "email") "Email"
                                else "Phone Number"
                            )
                        },
                        placeholder = {
                            Text(
                                if (selectedOption == "email")
                                    "Enter your email"
                                else
                                    "Enter your phone number"
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(0.90f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = TextGreen,
                            placeholderColor = Color(0xCC1F2A1F),
                            backgroundColor = SecondaryGreen,
                            cursorColor = PrimaryGreen,
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = SecondaryGreen
                        ),
                    )
                    var userFullName by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = userFullName,
                        onValueChange = { newText -> userFullName = newText },
                        label = { Text("Full Name") },
                        placeholder = { Text(text = "Enter your First, Middle and Last Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(0.90f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = TextGreen,
                            placeholderColor = Color(0xCC1F2A1F),
                            backgroundColor = SecondaryGreen,
                            cursorColor = PrimaryGreen,
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = SecondaryGreen
                        )
                    )

                    var username by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = username,
                        onValueChange = { newText -> username = newText },
                        label = { Text("Username") },
                        placeholder = { Text(text = "Enter your username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(0.90f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = TextGreen,
                            placeholderColor = Color(0xCC1F2A1F),
                            backgroundColor = SecondaryGreen,
                            cursorColor = PrimaryGreen,
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = SecondaryGreen
                        )
                    )

                    var password by remember { mutableStateOf("") }
                    var passwordVisible by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { newText -> password = newText },
                        label = { Text("Password") },
                        placeholder = { Text("Enter your password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image =
                                if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            Icon(
                                imageVector = image,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(0.90f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = TextGreen,
                            placeholderColor = Color(0xCC1F2A1F),
                            backgroundColor = SecondaryGreen,
                            cursorColor = PrimaryGreen,
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = SecondaryGreen
                        )
                    )
                    var secondPassword by remember { mutableStateOf("") }
                    var secondPasswordVisible by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = secondPassword,
                        onValueChange = { newText -> secondPassword = newText },
                        label = { Text("Confirm Password") },
                        placeholder = { Text("Enter your password again") },
                        singleLine = true,
                        visualTransformation = if (secondPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image =
                                if (secondPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            Icon(
                                imageVector = image,
                                contentDescription = if (secondPasswordVisible) "Hide password" else "Show password",
                                modifier = Modifier.clickable {
                                    secondPasswordVisible = !secondPasswordVisible
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(0.90f),
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


                Spacer(modifier = Modifier.height(72.dp))

                Button (
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(0.40f),
                    colors = ButtonColors(
                        containerColor = ButtonYellow,
                        contentColor = TextGreen,
                        disabledContainerColor = Color.LightGray,
                        disabledContentColor = Color.Gray
                    )
                ) {
                    Text(
                        "Sign up",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = TextGreen)) {
                            append("Have an account? ")
                        }
                        withStyle(style = SpanStyle(color = PrimaryGreen)) {
                            append("Login")
                        }
                    },
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .fillMaxWidth(0.85f)
                )
            }

        }
    )
}
