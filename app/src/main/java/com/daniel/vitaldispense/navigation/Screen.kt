package com.daniel.vitaldispense.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Patients : Screen("patients")
    object Dispensers : Screen("dispensers")
    object Alerts : Screen("alerts")
    object Settings : Screen("settings")
    object Invitaciones : Screen("invitaciones")
    object DetalleMedicamento : Screen("detalle/{medicamentoId}") {
        fun createRoute(medicamentoId: String) = "detalle/$medicamentoId"
    }
}
