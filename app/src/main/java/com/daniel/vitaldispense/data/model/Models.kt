package com.daniel.vitaldispense.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/**
 * Modelo de Usuario sincronizado con Firestore.
 */
data class Usuario(
    @get:PropertyName("uid") @set:PropertyName("uid") var uid: String? = null,
    @get:PropertyName("nombre") @set:PropertyName("nombre") var nombre: String? = null,
    @get:PropertyName("correo") @set:PropertyName("correo") var correo: String? = null,
    @get:PropertyName("rol") @set:PropertyName("rol") var rol: String? = null,
    @get:PropertyName("fotoPerfil") @set:PropertyName("fotoPerfil") var fotoPerfil: String? = null
)

/**
 * Modelo de Invitación sincronizado con la colección 'invitaciones'.
 */
data class Invitacion(
    @get:PropertyName("correo") @set:PropertyName("correo") var correo: String? = null,
    @get:PropertyName("estado") @set:PropertyName("estado") var estado: String? = null,
    @get:PropertyName("fechaInvitacion") @set:PropertyName("fechaInvitacion") var fechaInvitacion: Timestamp? = null,
    @get:PropertyName("rolInvitado") @set:PropertyName("rolInvitado") var rolInvitado: String? = null,
    @get:PropertyName("id_emisor") @set:PropertyName("id_emisor") var idEmisor: String? = null
)

/**
 * Modelo de Medicamento.
 */
data class Medicamento(
    val id: String = "",
    val nombre: String = "",
    val dosis: String = "",
    val horario: String = "",
    val tomado: Boolean = false,
    val icono: String = ""
)
