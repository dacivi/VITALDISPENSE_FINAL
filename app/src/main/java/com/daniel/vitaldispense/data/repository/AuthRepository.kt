package com.daniel.vitaldispense.data.repository

import android.util.Log
import com.daniel.vitaldispense.data.model.Usuario
import com.daniel.vitaldispense.features.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de Autenticación encargado de la lógica con Firebase Auth y Firestore.
 */
class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun getCurrentUser() = firebaseAuth.currentUser

    /**
     * Inicia sesión y recupera los datos del perfil desde la colección 'usuarios'.
     */
    suspend fun login(email: String, pass: String): AuthResult<Usuario> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: return AuthResult.Error("ID de usuario no encontrado")
            
            val userDoc = firestore.collection("usuarios").document(uid).get().await()
            val usuario = userDoc.toObject(Usuario::class.java)
            
            usuario?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error("El perfil no existe en la base de datos")
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Fallo al iniciar sesión")
        }
    }

    /**
     * Registro de usuario:
     * 1. Verifica invitación por 'correo' (Normalizado).
     * 2. Crea credenciales en Firebase Auth.
     * 3. Crea documento en 'usuarios' usando el UID de Auth como ID de documento.
     */
    suspend fun register(nombre: String, email: String, pass: String): AuthResult<Usuario> {
        val emailNormalizado = email.trim().lowercase()
        Log.d("DEBUG_VITAL", "Buscando correo: $emailNormalizado")

        return try {
            // 1. Validar invitación en la colección 'invitaciones'
            // Usamos 'correo' según la estructura confirmada de Firestore
            val snapshot = firestore.collection("invitaciones")
                .whereEqualTo("correo", emailNormalizado)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Log.d("DEBUG_VITAL", "Documento no encontrado para: $emailNormalizado")
                return AuthResult.Error("No existe una invitación para este correo electrónico")
            }

            Log.d("DEBUG_VITAL", "Invitación encontrada. Procediendo con el registro.")

            // 2. Crear usuario en Auth
            val authResult = firebaseAuth.createUserWithEmailAndPassword(emailNormalizado, pass).await()
            val uid = authResult.user?.uid ?: return AuthResult.Error("No se pudo generar el identificador único")

            // 3. Crear documento en la colección 'usuarios' con el mismo UID
            val nuevoUsuario = Usuario(
                uid = uid,
                nombre = nombre,
                correo = emailNormalizado,
                rol = "paciente", // Rol inicial por defecto
                fotoPerfil = null
            )

            firestore.collection("usuarios").document(uid).set(nuevoUsuario).await()
            
            AuthResult.Success(nuevoUsuario)
        } catch (e: Exception) {
            Log.e("DEBUG_VITAL", "Error crítico durante el registro: ${e.message}")
            AuthResult.Error(e.localizedMessage ?: "Error crítico durante el registro")
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}
