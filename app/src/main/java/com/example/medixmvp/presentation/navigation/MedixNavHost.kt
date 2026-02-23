package com.example.medixmvp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medixmvp.presentation.screens.AppointmentsScreen
import com.example.medixmvp.presentation.screens.ConversationScreen
import com.example.medixmvp.presentation.screens.SettingsScreen
import com.example.medixmvp.presentation.viewmodel.ConversationViewModel

@Composable
fun MedixNavHost() {
    val navController = rememberNavController()
    val vm: ConversationViewModel = hiltViewModel()
    NavHost(navController = navController, startDestination = "conversation") {
        composable("conversation") {
            ConversationScreen(
                viewModel = vm,
                onOpenSettings = { navController.navigate("settings") },
                onOpenAppointments = { navController.navigate("appointments") }
            )
        }
        composable("settings") { SettingsScreen(vm) { navController.popBackStack() } }
        composable("appointments") { AppointmentsScreen(vm) { navController.popBackStack() } }
    }
}
