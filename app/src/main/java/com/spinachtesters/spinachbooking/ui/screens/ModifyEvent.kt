package com.spinachtesters.spinachbooking.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.spinachtesters.spinachbooking.ui.viewmodels.AddEventViewModel

@Composable
fun ModifyEventScreen(
	eventId: String?,
	navController: NavController,
	viewModel: AddEventViewModel = viewModel()
) {
	AddEventScreen(
		navController = navController,
		viewModel = viewModel,
		eventId = eventId,
		isModifyMode = true
	)
}