package com.spinachtesters.spinachbooking.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
@Preview
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}

@Composable
fun LoginScreen(navController: NavController) {
    Scaffold(
        topBar = {TitleTopBar()},
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Login",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp,
                    color = TextGreen,
                    modifier = Modifier
                        .padding(vertical = 24.dp, )
                )

                Text (
                    "You can login with username, email, or phone number",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextGreen,
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .fillMaxWidth(0.85f)
                )

                var userInfo by remember {mutableStateOf("")}

                OutlinedTextField(
                    value = userInfo,
                    onValueChange = {newText -> userInfo = newText},
                    label = { Text("Username")},
                    placeholder = { Text( text = "Enter your username")},
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

                Spacer(modifier = Modifier.height(24.dp))

                var password by remember {mutableStateOf("")}
                var passwordVisible by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = password,
                    onValueChange = { newText -> password = newText },
                    label = { Text("Password") },
                    placeholder = { Text("Enter your password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
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
                        "Login",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = TextGreen)) {
                            append("No Account? ")
                        }
                        withStyle(style = SpanStyle(color = PrimaryGreen)) {
                            append("Register")
                        }
                    },
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .fillMaxWidth(0.85f)
                        .clickable {
                            navController.navigate("signup") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                )
            }

        }
    )
}