package com.daniel.vitaldispense.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.daniel.vitaldispense.features.home.HomeScreen
import com.daniel.vitaldispense.features.paciente.DetalleMedicamentoScreen
import com.daniel.vitaldispense.features.paciente.PacienteDashboardScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Patients : Screen("patients")
    object Dispensers : Screen("dispensers")
    object Alerts : Screen("alerts")
    object Settings : Screen("settings")
    object DetalleMedicamento : Screen("detalle/{medicamentoId}") {
        fun createRoute(medicamentoId: String) = "detalle/$medicamentoId"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.Patients.route) {
            PacienteDashboardScreen(onMedicamentoClick = { medicamento ->
                navController.navigate(Screen.DetalleMedicamento.createRoute(medicamento.id))
            })
        }
        composable(Screen.Dispensers.route) { /* TODO: Dispensers Screen */ }
        composable(Screen.Alerts.route) { /* TODO: Alerts Screen */ }
        composable(Screen.Settings.route) { /* TODO: Settings Screen */ }
        
        composable(Screen.DetalleMedicamento.route) { backStackEntry ->
            val medicamentoId = backStackEntry.arguments?.getString("medicamentoId") ?: ""
            DetalleMedicamentoScreen(
                medicamentoId = medicamentoId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
