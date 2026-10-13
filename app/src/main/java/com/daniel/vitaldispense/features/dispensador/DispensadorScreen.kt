package com.daniel.vitaldispense.features.dispensador

import com.daniel.vitaldispense.ui.theme.*

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==========================================
// 1. MODELO DE DATOS
// ==========================================

data class RegistroDosis(
    val paciente: String,
    val dosis: String,
    val dia: String,
    val timestamp: Long = System.currentTimeMillis(),
    val pastillaDetectada: Boolean
)

enum class EstadoCompuerta(val descripcion: String) {
    ABIERTA("Abierta"),
    CERRADA("Cerrada")
}

val DIAS_SEMANA = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

data class DispensadorUiState(
    val diaIndex: Int = 0,
    val estadoCompuerta: EstadoCompuerta = EstadoCompuerta.CERRADA,
    val nombrePaciente: String = "Daniel Martínez",
    val dosisActual: String = "500 mg - 1 Comprimido",
    val ultimoRegistro: RegistroDosis? = null,
    val mensajeNotificacion: String? = null
) {
    val diaActualNombre: String
        get() = DIAS_SEMANA.getOrElse(diaIndex) { "Lunes" }
}

// ==========================================
// 2. CAPA LÓGICA (VIEWMODEL CON PARTICLE API)
// ==========================================

class DispensadorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DispensadorUiState())
    val uiState: StateFlow<DispensadorUiState> = _uiState.asStateFlow()

    private val client = OkHttpClient()

    fun onNombrePacienteChange(nuevoNombre: String) {
        _uiState.update { it.copy(nombrePaciente = nuevoNombre) }
    }

    fun onDosisActualChange(nuevaDosis: String) {
        _uiState.update { it.copy(dosisActual = nuevaDosis) }
    }

    /**
     * Función genérica para enviar llamadas POST a las funciones alojadas en Particle Cloud.
     */
    private fun ejecutarComandoParticle(
        funcionName: String,
        arg: String = "",
        onSuccessCallback: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = "https://api.particle.io/v1/devices/$DEVICE_ID/$funcionName"

                val formBodyBuilder = FormBody.Builder()
                    .add("access_token", ACCESS_TOKEN)

                if (arg.isNotBlank()) {
                    formBodyBuilder.add("args", arg)
                }

                val request = Request.Builder()
                    .url(url)
                    .post(formBodyBuilder.build())
                    .build()

                Log.d(TAG, "Enviando petición POST a Particle API: $url")

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        Log.d(TAG, "Éxito en Particle ($funcionName): $responseBody")
                        withContext(Dispatchers.Main) {
                            onSuccessCallback()
                            _uiState.update { state ->
                                state.copy(
                                    mensajeNotificacion = "Comando '$funcionName' enviado exitosamente al dispositivo Particle IoT"
                                )
                            }
                        }
                    } else {
                        Log.e(TAG, "Error HTTP en Particle ($funcionName) - Código ${response.code}: $responseBody")
                        withContext(Dispatchers.Main) {
                            _uiState.update { state ->
                                state.copy(
                                    mensajeNotificacion = "ALERTA: Error Particle ($funcionName) - Código HTTP ${response.code}"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción en llamada a Particle ($funcionName): ${e.localizedMessage}", e)
                withContext(Dispatchers.Main) {
                    _uiState.update { state ->
                        state.copy(
                            mensajeNotificacion = "ALERTA: Error de conexión con Particle: ${e.localizedMessage}"
                        )
                    }
                }
            }
        }
    }

    /**
     * Llama remotamente a la función Particle 'avanzarDia'.
     */
    fun avanzarDiaRemoto() {
        ejecutarComandoParticle("avanzarDia") {
            avanzarDia()
        }
    }

    /**
     * Llama remotamente a la función Particle 'dispensar'.
     */
    fun alternarCompuertaRemoto() {
        ejecutarComandoParticle("dispensar") {
            alternarCompuerta()
        }
    }

    fun avanzarDia() {
        _uiState.update { currentState ->
            val siguienteIndex = (currentState.diaIndex + 1) % DIAS_SEMANA.size
            val nuevoDia = DIAS_SEMANA[siguienteIndex]
            Log.d(TAG, "Carrusel avanzado al día índice: $siguienteIndex ($nuevoDia)")
            currentState.copy(
                diaIndex = siguienteIndex,
                mensajeNotificacion = "Carrusel rotó a $nuevoDia"
            )
        }
    }

    fun alternarCompuerta() {
        _uiState.update { currentState ->
            val nuevoEstado = if (currentState.estadoCompuerta == EstadoCompuerta.ABIERTA) {
                EstadoCompuerta.CERRADA
            } else {
                EstadoCompuerta.ABIERTA
            }
            Log.d(TAG, "Estado de compuerta cambiado a: ${nuevoEstado.descripcion}")
            currentState.copy(
                estadoCompuerta = nuevoEstado,
                mensajeNotificacion = "Compuerta ${nuevoEstado.descripcion.lowercase()}"
            )
        }
    }

    fun registrarToma(sensorDetectoCaida: Boolean) {
        val currentState = _uiState.value
        val paciente = currentState.nombrePaciente.ifBlank { "Paciente Desconocido" }
        val dosis = currentState.dosisActual.ifBlank { "Sin Dosis Especificada" }
        val dia = currentState.diaActualNombre

        val nuevoRegistro = RegistroDosis(
            paciente = paciente,
            dosis = dosis,
            dia = dia,
            timestamp = System.currentTimeMillis(),
            pastillaDetectada = sensorDetectoCaida
        )

        Log.d(TAG, "================ REGISTRO DE DOSIS ================")
        Log.d(TAG, "Paciente:           ${nuevoRegistro.paciente}")
        Log.d(TAG, "Dosis:              ${nuevoRegistro.dosis}")
        Log.d(TAG, "Día Carrusel:       ${nuevoRegistro.dia}")
        Log.d(TAG, "Timestamp:          ${nuevoRegistro.timestamp}")
        Log.d(TAG, "Pastilla Detectada: ${nuevoRegistro.pastillaDetectada}")
        Log.d(TAG, "===================================================")

        _uiState.update { state ->
            state.copy(
                ultimoRegistro = nuevoRegistro,
                mensajeNotificacion = if (sensorDetectoCaida) {
                    "¡Toma confirmada por sensor óptico para $paciente!"
                } else {
                    "¡ALERTA! El sensor óptico NO detectó la caída de la pastilla."
                }
            )
        }
    }

    companion object {
        private const val TAG = "DispensadorViewModel"
        private const val DEVICE_ID = "24003b000b47313037363132"
        private const val ACCESS_TOKEN = "199f81447240af76b7896bbe24b0964e8ff5933c"
    }
}

// ==========================================
// 3. CAPA DE INTERFAZ (JETPACK COMPOSE)
// ==========================================

@Composable
fun DispensadorScreen(
    viewModel: DispensadorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val backgroundColor = BackgroundSecondary
    val primaryBlue = BrandPrimary
    val darkTitle = TextPrimary
    val successGreen = SuccessDark
    val criticalRed = ErrorRed
    val warningAmber = WarningOrange

    Scaffold(containerColor = backgroundColor) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            // ── Header (Estilo HomeScreen) ────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dispensador IoT",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = darkTitle
                        )
                        Text(
                            text = "Control Remoto Particle · Carrusel 7 Días",
                            style = MaterialTheme.typography.bodyMedium,
                            color = primaryBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Surface(
                        color = Color.White,
                        shape = CircleShape,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(successGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Particle Cloud",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = primaryBlue
                            )
                        }
                    }
                }
            }

            // ── Resumen rápido (4 ResumenCards) ──────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ResumenCardDispensador(
                        label = "Día Carrusel",
                        valor = "${uiState.diaIndex + 1}/7",
                        color = primaryBlue,
                        modifier = Modifier.weight(1f)
                    )
                    ResumenCardDispensador(
                        label = "Compuerta",
                        valor = uiState.estadoCompuerta.descripcion,
                        color = if (uiState.estadoCompuerta == EstadoCompuerta.ABIERTA) criticalRed else successGreen,
                        modifier = Modifier.weight(1f)
                    )
                    ResumenCardDispensador(
                        label = "Paciente",
                        valor = uiState.nombrePaciente.split(" ").firstOrNull() ?: "Daniel",
                        color = darkTitle,
                        modifier = Modifier.weight(1f)
                    )
                    ResumenCardDispensador(
                        label = "Sensor",
                        valor = if (uiState.ultimoRegistro?.pastillaDetectada == false) "Alerta" else "OK",
                        color = if (uiState.ultimoRegistro?.pastillaDetectada == false) criticalRed else warningAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Notificación Alerta / Notificación ───────────────────────────
            if (uiState.mensajeNotificacion != null) {
                item {
                    val esAlerta = uiState.mensajeNotificacion?.contains("ALERTA") == true
                    val bgColor = if (esAlerta) ErrorSoft else SuccessSoft
                    val borderColor = if (esAlerta) criticalRed else successGreen

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(borderColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (esAlerta) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = borderColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.mensajeNotificacion ?: "",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = borderColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // ── Estado del Hardware (Carrusel y Compuerta) ────────────────────
            item {
                SectionHeaderDispensador(
                    titulo = "Estado del Hardware",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card Día Carrusel
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = primaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Día del Carrusel",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.diaActualNombre,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = darkTitle
                            )
                            Text(
                                text = "Posición ${uiState.diaIndex + 1} de 7",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (uiState.diaIndex + 1) / 7f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = primaryBlue,
                                trackColor = DividerGray
                            )
                        }
                    }

                    // Card Compuerta
                    val compuertaAbierta = uiState.estadoCompuerta == EstadoCompuerta.ABIERTA
                    val colorCompuerta by animateColorAsState(
                        targetValue = if (compuertaAbierta) criticalRed else successGreen,
                        label = "ColorCompuerta"
                    )

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (compuertaAbierta) Icons.Default.LockOpen else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = colorCompuerta,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Compuerta",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.estadoCompuerta.descripcion,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colorCompuerta
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = colorCompuerta.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (compuertaAbierta) "Desbloqueada" else "Protegida",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorCompuerta
                                )
                            }
                        }
                    }
                }
            }

            // ── Programación de Paciente y Dosis ──────────────────────────────
            item {
                SectionHeaderDispensador(
                    titulo = "Programación",
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.nombrePaciente,
                            onValueChange = { viewModel.onNombrePacienteChange(it) },
                            label = { Text("Paciente", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = primaryBlue)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBlue,
                                unfocusedBorderColor = DividerGray
                            )
                        )

                        OutlinedTextField(
                            value = uiState.dosisActual,
                            onValueChange = { viewModel.onDosisActualChange(it) },
                            label = { Text("Dosis Asignada", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Medication, contentDescription = null, tint = primaryBlue)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBlue,
                                unfocusedBorderColor = DividerGray
                            )
                        )
                    }
                }
            }

            // ── Simulación y Control de Sensores IoT ───────────────────────────
            item {
                SectionHeaderDispensador(
                    titulo = "Control Remoto Particle IoT",
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sensors, contentDescription = null, tint = primaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Comandos de Hardware Físico",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = darkTitle
                            )
                        }

                        // Botón a) Sensor: Avanzar Día (Llama a avanzarDiaRemoto)
                        OutlinedButton(
                            onClick = { viewModel.avanzarDiaRemoto() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = primaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sensor: Avanzar Día", color = primaryBlue, fontWeight = FontWeight.Bold)
                        }

                        // Botón b) Sensor: Alternar Compuerta (Llama a alternarCompuertaRemoto)
                        OutlinedButton(
                            onClick = { viewModel.alternarCompuertaRemoto() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.estadoCompuerta == EstadoCompuerta.ABIERTA) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = primaryBlue
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Sensor: Alternar Compuerta (${if (uiState.estadoCompuerta == EstadoCompuerta.ABIERTA) "Cerrar" else "Abrir"})",
                                color = primaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Botón c) Sensor: Pastilla Cayó
                        Button(
                            onClick = { viewModel.registrarToma(sensorDetectoCaida = true) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = successGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sensor: Pastilla Cayó (Confirmación Óptica)", fontWeight = FontWeight.Bold)
                        }

                        // Botón d) Sensor: Falsa Alarma
                        Button(
                            onClick = { viewModel.registrarToma(sensorDetectoCaida = false) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = criticalRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sensor: Falsa Alarma / No Cayó", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Último Registro Generado ───────────────────────────────────────
            uiState.ultimoRegistro?.let { registro ->
                item {
                    SectionHeaderDispensador(
                        titulo = "Último Evento Registrado",
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 4.dp)
                    )
                }

                item {
                    val isSuccess = registro.pastillaDetectada
                    val cardBg = if (isSuccess) SuccessSoft else ErrorSoft
                    val statusColor = if (isSuccess) successGreen else criticalRed

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = statusColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSuccess) "Dosis Confirmada por Sensor Óptico" else "Alerta: No se Detectó Caída de Pastilla",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = statusColor
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Paciente: ${registro.paciente}", fontSize = 13.sp, color = darkTitle, fontWeight = FontWeight.Medium)
                            Text("• Dosis: ${registro.dosis}", fontSize = 13.sp, color = darkTitle)
                            Text("• Día carrusel: ${registro.dia}", fontSize = 13.sp, color = darkTitle)
                            Text(
                                "• Hora: ${SimpleDateFormat("HH:mm:ss - dd/MM/yyyy", Locale.getDefault()).format(Date(registro.timestamp))}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Componentes de UI ─────────────────────────────────────────────────────────

@Composable
fun ResumenCardDispensador(label: String, valor: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SectionHeaderDispensador(titulo: String, modifier: Modifier = Modifier) {
    Text(
        text = titulo,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    )
}
