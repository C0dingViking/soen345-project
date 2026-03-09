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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.spinachtesters.spinachbooking.ui.viewmodels.LoginViewModel

@Composable
@Preview
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TitleTopBar() },
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
                        .padding(vertical = 24.dp)
                )

                Text(
                    "You can login with username, email, or phone number",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextGreen,
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .fillMaxWidth(0.85f)
                )

                OutlinedTextField(
                    value = uiState.identifier,
                    onValueChange = viewModel::onIdentifierChanged,
                    label = { Text("Username, Email, or Phone") },
                    placeholder = { Text(text = "Enter your username, email, or phone") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .testTag("login_identifier_input"),
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

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
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
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .testTag("login_password_input"),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = TextGreen,
                        placeholderColor = Color(0xCC1F2A1F),
                        backgroundColor = SecondaryGreen,
                        cursorColor = PrimaryGreen,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = SecondaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color(0xFFB00020),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth(0.90f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (uiState.isAuthenticated) {
                    Text(
                        text = "Login successful",
                        color = PrimaryGreen,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(0.90f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }


                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { viewModel.login() },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth(0.40f)
                        .testTag("login_submit_button"),
                    colors = ButtonColors(
                        containerColor = ButtonYellow,
                        contentColor = TextGreen,
                        disabledContainerColor = Color.LightGray,
                        disabledContentColor = Color.Gray
                    )
                ) {
                    Text(
                        if (uiState.isLoading) "Logging in..." else "Login",
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