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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.spinachtesters.spinachbooking.ui.viewmodels.SignUpViewModel

@Composable
@Preview
fun SignUpScreenPreview() {
    SignUpScreen(navController = rememberNavController())
}

@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: SignUpViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedOption by remember { mutableStateOf("email") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.consumeSuccess()
            navController.navigate("login") {
                popUpTo("signup") { inclusive = true }
            }
        }
    }

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
                        Text("email", color = if (selectedOption == "email") Color.White else TextGreen)
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

                OutlinedTextField(
                    value = if (selectedOption == "email") uiState.email else uiState.phoneNumber,
                    onValueChange = {
                        if (selectedOption == "email") viewModel.onEmailChanged(it) else viewModel.onPhoneNumberChanged(it)
                    },
                    label = { Text(if (selectedOption == "email") "Email" else "Phone Number") },
                    placeholder = { Text(if (selectedOption == "email") "Enter your email" else "Enter your phone number") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .testTag("signup_contact_input"),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = TextGreen,
                        placeholderColor = Color(0xCC1F2A1F),
                        backgroundColor = SecondaryGreen,
                        cursorColor = PrimaryGreen,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = SecondaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.fullName,
                    onValueChange = viewModel::onFullNameChanged,
                    label = { Text("Full Name") },
                    placeholder = { Text("Enter your First, Middle and Last Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .testTag("signup_fullname_input"),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = TextGreen,
                        placeholderColor = Color(0xCC1F2A1F),
                        backgroundColor = SecondaryGreen,
                        cursorColor = PrimaryGreen,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = SecondaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = viewModel::onUsernameChanged,
                    label = { Text("Username") },
                    placeholder = { Text("Enter your username") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .testTag("signup_username_input"),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = TextGreen,
                        placeholderColor = Color(0xCC1F2A1F),
                        backgroundColor = SecondaryGreen,
                        cursorColor = PrimaryGreen,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = SecondaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = { Text("Password") },
                    placeholder = { Text("Enter your password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .testTag("signup_password_input"),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = TextGreen,
                        placeholderColor = Color(0xCC1F2A1F),
                        backgroundColor = SecondaryGreen,
                        cursorColor = PrimaryGreen,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = SecondaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChanged,
                    label = { Text("Confirm Password") },
                    placeholder = { Text("Enter your password again") },
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                            modifier = Modifier.clickable { confirmPasswordVisible = !confirmPasswordVisible }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .testTag("signup_confirm_password_input"),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = TextGreen,
                        placeholderColor = Color(0xCC1F2A1F),
                        backgroundColor = SecondaryGreen,
                        cursorColor = PrimaryGreen,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = SecondaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color(0xFFB00020),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth(0.90f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier
                            .background(SecondaryGreen, RoundedCornerShape(50))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (!uiState.isOrganizer) PrimaryGreen else Color.Transparent)
                                .clickable { viewModel.onIsOrganizerChanged(false) }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "User",
                                color = if (!uiState.isOrganizer) Color.White else TextGreen
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (uiState.isOrganizer) PrimaryGreen else Color.Transparent)
                                .clickable { viewModel.onIsOrganizerChanged(true) }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Organizer",
                                color = if (uiState.isOrganizer) Color.White else TextGreen
                            )
                        }
                    }
                }

                Button(
                    onClick = { viewModel.signUp(useEmail = selectedOption == "email") },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth(0.40f)
                        .testTag("signup_submit_button"),
                    colors = ButtonColors(
                        containerColor = ButtonYellow,
                        contentColor = TextGreen,
                        disabledContainerColor = Color.LightGray,
                        disabledContentColor = Color.Gray
                    )
                ) {
                    Text(
                        if (uiState.isLoading) "Signing up..." else "Sign up",
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
                        .clickable {
                            navController.navigate("login") {
                                popUpTo("signup") { inclusive = true }
                            }
                        }
                )
            }

        }
    )
}
