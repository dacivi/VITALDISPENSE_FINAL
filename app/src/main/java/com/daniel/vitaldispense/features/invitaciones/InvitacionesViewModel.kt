package com.daniel.vitaldispense.features.invitaciones

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daniel.vitaldispense.data.model.Invitacion
import com.daniel.vitaldispense.features.auth.AuthResult
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class InvitacionesViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _invitacionesState = MutableStateFlow<AuthResult<List<Invitacion>>>(AuthResult.Loading)
    val invitacionesState: StateFlow<AuthResult<List<Invitacion>>> = _invitacionesState

    // Estado para la acción de enviar invitación
    private val _envioState = MutableStateFlow<AuthResult<String>>(AuthResult.Idle)
    val envioState: StateFlow<AuthResult<String>> = _envioState

    init {
        obtenerInvitacionesEnviadas()
    }

    fun obtenerInvitacionesEnviadas() {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            _invitacionesState.value = AuthResult.Error("Usuario no autenticado")
            return
        }

        viewModelScope.launch {
            _invitacionesState.value = AuthResult.Loading
            try {
                val snapshot = firestore.collection("invitaciones")
                    .whereEqualTo("id_emisor", currentUid)
                    .get()
                    .await()

                val lista = snapshot.documents.mapNotNull { it.toObject(Invitacion::class.java) }
                _invitacionesState.value = AuthResult.Success(lista)
            } catch (e: Exception) {
                Log.e("InvitacionesVM", "Error al obtener invitaciones: ${e.message}", e)
                _invitacionesState.value = AuthResult.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }

    fun enviarInvitacion(correo: String, onEmailReady: (String) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _envioState.value = AuthResult.Loading
            try {
                val nuevaInvitacion = Invitacion(
                    correo = correo,
                    estado = "pendiente",
                    fechaInvitacion = Timestamp.now(),
                    rolInvitado = "paciente",
                    idEmisor = currentUid // Aseguramos que guarde quién la envía
                )

                // Guardar en Firestore usando el correo como ID para evitar duplicados si se desea
                // o generar un ID automático. Usaremos ID automático para permitir re-enviar.
                firestore.collection("invitaciones").add(nuevaInvitacion).await()
                
                _envioState.value = AuthResult.Success("Invitación guardada")
                onEmailReady(correo)
                obtenerInvitacionesEnviadas() // Refrescar lista
            } catch (e: Exception) {
                Log.e("InvitacionesVM", "Error al enviar invitación: ${e.message}")
                _envioState.value = AuthResult.Error(e.localizedMessage ?: "Error al guardar invitación")
            }
        }
    }
    
    fun resetEnvioState() {
        _envioState.value = AuthResult.Idle
    }
}
