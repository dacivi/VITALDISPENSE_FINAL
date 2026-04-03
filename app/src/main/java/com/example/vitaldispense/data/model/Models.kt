package com.daniel.vitaldispense.data.model

import com.google.firebase.Timestamp

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val rol: String = "",
    val fcmToken: String = ""
)

data class Paciente(
    val idPaciente: String = "",
    val nombrePaciente: String = "",
    val edad: Int = 0,
    val tipoSangre: String = "",
    val idDispensador: String = "",
    val padecimientos: List<String> = emptyList()
)

data class Dispensador(
    val idFisico: String = "",
    val estado: Boolean = false,
    val ultimaConexion: Timestamp? = null,
    val usuarioAsignado: String = ""
)

data class Medicamento(
    val id: String = "",
    val nombre: String = "",
    val dosis: String = "",
    val horario: String = "",
    val tomado: Boolean = false,
    val icono: String = "" 
)
