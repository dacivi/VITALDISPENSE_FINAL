package com.daniel.vitaldispense.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daniel.vitaldispense.data.model.Usuario
import com.daniel.vitaldispense.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _authState = MutableStateFlow<AuthResult<Usuario>>(AuthResult.Idle)
    val authState: StateFlow<AuthResult<Usuario>> = _authState

    val currentUser: FirebaseUser? get() = repository.getCurrentUser()

    fun login(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            _authState.value = AuthResult.Error("Campos obligatorios")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            _authState.value = repository.login(email, pass)
        }
    }

    fun register(nombre: String, email: String, pass: String) {
        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            _authState.value = AuthResult.Error("Todos los campos son obligatorios")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            _authState.value = repository.register(nombre, email, pass)
        }
    }

    fun resetState() {
        _authState.value = AuthResult.Idle
    }
}
