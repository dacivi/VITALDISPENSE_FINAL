package com.daniel.vitaldispense.features.hardware

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- Modelos de Datos IoT (Asegurados en el paquete) ---
data class SensorData(
    val temperature: Float = 0f,
    val humidity: Float = 0f,
    val history: List<Float> = emptyList()
)

data class Compartimento(
    val id: Int,
    val medicamento: String,
    val dosis: String,
    val cantidad: Int,
    val stockMinimo: Int = 5
)

sealed class HardwareUiState {
    object Loading : HardwareUiState()
    data class Success(
        val isOnline: Boolean,
        val sensors: SensorData,
        val inventario: List<Compartimento>,
        val isDispensing: Boolean = false
    ) : HardwareUiState()
    data class Error(val message: String) : HardwareUiState()
}

class HardwareViewModel : ViewModel() {
    // Nuevas referencias independientes separadas por responsabilidad
    private val sensoresRef = Firebase.database.getReference("sensores")
    private val actuadoresRef = Firebase.database.getReference("actuadores")

    private val _uiState = MutableStateFlow<HardwareUiState>(HardwareUiState.Loading)
    val uiState: StateFlow<HardwareUiState> = _uiState.asStateFlow()

    private val _humedadActual = MutableStateFlow(0)
    val humedadActual: StateFlow<Int> = _humedadActual.asStateFlow()

    private val _isDispensing = MutableStateFlow(false)
    val isDispensing: StateFlow<Boolean> = _isDispensing.asStateFlow()

    private val humidityHistory = mutableListOf<Float>()

    init {
        conectarFirebase()
    }

    private fun conectarFirebase() {
        // 1. Escuchar el estado del comando de dispensación (Auto-apagado desde el hardware)
        actuadoresRef.child("dispensar_comando").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dispensing = snapshot.getValue(Boolean::class.java) ?: false
                _isDispensing.value = dispensing
                
                // Actualizamos el estado de la UI si ya estamos en modo Success
                val currentState = _uiState.value
                if (currentState is HardwareUiState.Success) {
                    _uiState.value = currentState.copy(isDispensing = dispensing)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 2. Escuchar la rama de "sensores" para lecturas ambientales
        sensoresRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val humedadSnapshot = snapshot.child("humedad")
                    val humedad = when {
                        humedadSnapshot.exists() -> (humedadSnapshot.value as? Number)?.toInt() ?: 0
                        else -> 0
                    }

                    val tempSnapshot = snapshot.child("temperatura")
                    val temperatura = when {
                        tempSnapshot.exists() -> (tempSnapshot.value as? Number)?.toFloat() ?: 0f
                        else -> 0f
                    }

                    val onlineSnapshot = snapshot.child("estado")
                    val online = when {
                        onlineSnapshot.exists() -> (onlineSnapshot.value as? Boolean) ?: false
                        else -> true 
                    }

                    _humedadActual.value = humedad

                    if (humidityHistory.size >= 15) humidityHistory.removeAt(0)
                    humidityHistory.add(humedad.toFloat())

                    _uiState.value = HardwareUiState.Success(
                        isOnline = online,
                        sensors = SensorData(
                            temperature = temperatura,
                            humidity = humedad.toFloat(),
                            history = humidityHistory.toList()
                        ),
                        inventario = listOf(
                            Compartimento(1, "Paracetamol", "500mg", 12),
                            Compartimento(2, "Ibuprofeno", "400mg", 2),
                            Compartimento(3, "Metformina", "850mg", 20),
                            Compartimento(4, "Amlodipino", "5mg", 15)
                        ),
                        isDispensing = (uiState.value as? HardwareUiState.Success)?.isDispensing ?: _isDispensing.value
                    )
                } catch (e: Exception) {
                    _uiState.value = HardwareUiState.Error("Error en sensores: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _uiState.value = HardwareUiState.Error(error.message)
            }
        })
    }

    fun dispensarManualmente() {
        viewModelScope.launch {
            try {
                // Solo activamos el comando en Firebase. 
                // La UI se bloqueará reactivamente gracias al ValueEventListener.
                actuadoresRef.child("dispensar_comando").setValue(true)
            } catch (e: Exception) {
                _isDispensing.value = false
            }
        }
    }
}
