package com.daniel.vitaldispense.navigation

sealed class Screen(val route: String) {

    // Auth
    object Login      : Screen("login")
    object Register   : Screen("register")

    // Bottom Nav — 4 pestañas
    object Inicio     : Screen("inicio")          // Tab 1: Dashboard
    object Pacientes  : Screen("pacientes")       // Tab 2: Lista de pacientes
    object Dispensador: Screen("dispensador")     // Tab 3: Hardware IoT
    object Ajustes    : Screen("ajustes")         // Tab 4: Perfil / Turno

    // Sub-pantallas de Pacientes
    object AgregarPaciente : Screen("agregar_paciente")
    object DetallePaciente : Screen("detalle_paciente/{pacienteId}") {
        fun createRoute(pacienteId: String) = "detalle_paciente/$pacienteId"
    }

    // Sub-pantallas accesibles desde el nav pero fuera del bottom bar
    object Alertas       : Screen("alertas")        // Desde ícono campana en Home
    object Invitaciones  : Screen("invitaciones")   // Desde Perfil/Ajustes

    // Mantenidas por compatibilidad (DetalleMedicamento si se usa en el futuro)
    object DetalleMedicamento : Screen("detalle_medicamento/{medicamentoId}") {
        fun createRoute(medicamentoId: String) = "detalle_medicamento/$medicamentoId"
    }
}
