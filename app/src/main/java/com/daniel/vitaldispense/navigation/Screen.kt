package com.daniel.vitaldispense.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Inicio : Screen("inicio")
    object Tomas : Screen("tomas")
    object Resumen : Screen("resumen")
    object Dispensador : Screen("dispensador") // Nuevo apartado
    object Alertas : Screen("alertas")
    object Ajustes : Screen("ajustes")
    
    object DetalleMedicamento : Screen("detalle/{medicamentoId}") {
        fun createRoute(medicamentoId: String) = "detalle/$medicamentoId"
    }
}
