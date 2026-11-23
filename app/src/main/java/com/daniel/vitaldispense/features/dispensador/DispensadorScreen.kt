package com.daniel.vitaldispense.features.dispensador

import com.daniel.vitaldispense.ui.theme.*

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

data class NotificacionTemporal(
    val mensaje: String,
    val esError: Boolean,
    val id: Long = System.currentTimeMillis()
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
    val notificacion: NotificacionTemporal? = null,
    val enviandoComando: Boolean = false
) {
    val diaActualNombre: String
        get() = DIAS_SEMANA.getOrElse(diaIndex) { "Lunes" }
}

private val CardBorder = Color(0xFFE6E9EE)

// ==========================================
// 2. CAPA LÓGICA (VIEWMODEL CON PARTICLE API)
// ==========================================

class DispensadorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DispensadorUiState())
    val uiState: StateFlow<DispensadorUiState> = _uiState.asStateFlow()

    private val client = OkHttpClient()
    private var notificacionJob: Job? = null

    fun onNombrePacienteChange(nuevoNombre: String) {
        _uiState.update { it.copy(nombrePaciente = nuevoNombre) }
    }

    fun onDosisActualChange(nuevaDosis: String) {
        _uiState.update { it.copy(dosisActual = nuevaDosis) }
    }

    /**
     * Muestra un aviso temporal (3.5 s) y lo oculta automáticamente.
     */
    private fun mostrarNotificacionTemporal(mensaje: String, esError: Boolean) {
        notificacionJob?.cancel()
        _uiState.update {
            it.copy(notificacion = NotificacionTemporal(mensaje = mensaje, esError = esError))
        }
        notificacionJob = viewModelScope.launch {
            delay(3500L)
            _uiState.update { it.copy(notificacion = null) }
        }
    }

    /**
     * Envía llamadas POST a las funciones alojadas en Particle Cloud.
     */
    private fun ejecutarComandoParticle(
        funcionName: String,
        arg: String = "",
        notificarResultado: Boolean = false,
        onSuccessCallback: () -> String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(enviandoComando = true) }
            }
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
                            val mensajeExito = onSuccessCallback()
                            if (notificarResultado && mensajeExito != null) {
                                mostrarNotificacionTemporal(mensajeExito, esError = false)
                            }
                        }
                    } else {
                        Log.e(TAG, "Error HTTP en Particle ($funcionName) - Código ${response.code}: $responseBody")
                        withContext(Dispatchers.Main) {
                            if (notificarResultado) {
                                mostrarNotificacionTemporal(
                                    mensaje = "No se pudo avanzar el día (HTTP ${response.code})",
                                    esError = true
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción en llamada a Particle ($funcionName): ${e.localizedMessage}", e)
                withContext(Dispatchers.Main) {
                    if (notificarResultado) {
                        mostrarNotificacionTemporal(
                            mensaje = "Error de conexión con Particle Cloud",
                            esError = true
                        )
                    }
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(enviandoComando = false) }
                }
            }
        }
    }

    /**
     * Llama remotamente a 'avanzarDia' y muestra aviso temporal de éxito o error.
     */
    fun avanzarDiaRemoto() {
        ejecutarComandoParticle(
            funcionName = "avanzarDia",
            notificarResultado = true
        ) {
            val nuevoDia = avanzarDia()
            "Carrusel avanzado a $nuevoDia correctamente"
        }
    }

    /**
     * Llama remotamente a 'dispensar' para alternar la compuerta.
     */
    fun alternarCompuertaRemoto() {
        ejecutarComandoParticle(
            funcionName = "dispensar",
            notificarResultado = false
        ) {
            alternarCompuerta()
            null
        }
    }

    fun avanzarDia(): String {
        var nuevoDia = DIAS_SEMANA.first()
        _uiState.update { currentState ->
            val siguienteIndex = (currentState.diaIndex + 1) % DIAS_SEMANA.size
            nuevoDia = DIAS_SEMANA[siguienteIndex]
            Log.d(TAG, "Carrusel avanzado al día índice: $siguienteIndex ($nuevoDia)")
            currentState.copy(diaIndex = siguienteIndex)
        }
        return nuevoDia
    }

    fun alternarCompuerta() {
        _uiState.update { currentState ->
            val nuevoEstado = if (currentState.estadoCompuerta == EstadoCompuerta.ABIERTA) {
                EstadoCompuerta.CERRADA
            } else {
                EstadoCompuerta.ABIERTA
            }
            Log.d(TAG, "Estado de compuerta cambiado a: ${nuevoEstado.descripcion}")
            currentState.copy(estadoCompuerta = nuevoEstado)
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

        Log.d(TAG, "Registro guardado: $nuevoRegistro")

        _uiState.update { state ->
            state.copy(ultimoRegistro = nuevoRegistro)
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
    val compuertaAbierta = uiState.estadoCompuerta == EstadoCompuerta.ABIERTA

    Scaffold(containerColor = BackgroundSecondary) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 104.dp)
        ) {

            // ── Header (Mismo estilo que HomeScreen) ──────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dispensador IoT",
                            fontSize = 23.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.4).sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(BrandPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Particle Cloud · Carrusel 7 Días",
                                fontSize = 13.sp,
                                color = BrandPrimary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        color = Color.White,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(SuccessDark)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "En línea",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // ── Confirmación esporádica (solo al picar Avanzar Día) ───────────
            item {
                AnimatedVisibility(
                    visible = uiState.notificacion != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    uiState.notificacion?.let { notif ->
                        val bgColor = if (notif.esError) ErrorSoft else SuccessSoft
                        val accentColor = if (notif.esError) ErrorRed else SuccessDark

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp, bottom = 14.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = bgColor,
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(accentColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (notif.esError) Icons.Default.Close else Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = notif.mensaje,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = accentColor
                                )
                            }
                        }
                    }
                }
            }

            // ── Control Remoto (HASTA ARRIBA) ─────────────────────────────────
            item {
                SectionHeaderDispensador(
                    titulo = "Control Rápido",
                    badge = if (uiState.enviandoComando) "Enviando..." else "Particle IoT",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PrimarySoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Comandos de Hardware",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Acciona el carrusel y la compuerta en tiempo real",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Botón principal: Avanzar Día
                        Button(
                            onClick = { viewModel.avanzarDiaRemoto() },
                            enabled = !uiState.enviandoComando,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandPrimary,
                                contentColor = Color.White,
                                disabledContainerColor = BrandPrimary.copy(alpha = 0.5f),
                                disabledContentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            if (uiState.enviandoComando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Avanzar Día",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Botón secundario: Alternar Compuerta
                        OutlinedButton(
                            onClick = { viewModel.alternarCompuertaRemoto() },
                            enabled = !uiState.enviandoComando,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.45f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = PrimarySoft.copy(alpha = 0.45f),
                                contentColor = BrandPrimary
                            )
                        ) {
                            Icon(
                                imageVector = if (compuertaAbierta) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Alternar Compuerta (${if (compuertaAbierta) "Cerrar" else "Abrir"})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            // ── Resumen rápido (4 KPIs) ───────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ResumenCardDispensador(
                        label = "Día Carrusel",
                        valor = "${uiState.diaIndex + 1}/7",
                        color = BrandPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    ResumenCardDispensador(
                        label = "Compuerta",
                        valor = uiState.estadoCompuerta.descripcion,
                        color = if (compuertaAbierta) WarningOrange else BrandPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    ResumenCardDispensador(
                        label = "Paciente",
                        valor = uiState.nombrePaciente.split(" ").firstOrNull() ?: "Daniel",
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    ResumenCardDispensador(
                        label = "Sensor",
                        valor = if (uiState.ultimoRegistro?.pastillaDetectada == false) "Alerta" else "OK",
                        color = if (uiState.ultimoRegistro?.pastillaDetectada == false) ErrorRed else SuccessDark,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Estado del Hardware (Carrusel y Compuerta) ────────────────────
            item {
                SectionHeaderDispensador(
                    titulo = "Estado del Hardware",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card Día Carrusel
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(15.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Día Carrusel",
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
                                color = TextPrimary
                            )
                            Text(
                                text = "Posición ${uiState.diaIndex + 1} de 7",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { (uiState.diaIndex + 1) / 7f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(CircleShape),
                                color = BrandPrimary,
                                trackColor = PrimarySoft
                            )
                        }
                    }

                    // Card Compuerta
                    val colorCompuerta by animateColorAsState(
                        targetValue = if (compuertaAbierta) WarningOrange else BrandPrimary,
                        label = "ColorCompuerta"
                    )
                    val bgCompuerta = if (compuertaAbierta) WarningSoft else PrimarySoft

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(15.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (compuertaAbierta) Icons.Default.LockOpen else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = colorCompuerta,
                                    modifier = Modifier.size(16.dp)
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
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = bgCompuerta,
                                shape = CircleShape,
                                border = BorderStroke(1.dp, colorCompuerta.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = if (compuertaAbierta) "Desbloqueada" else "Protegida",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
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
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 6.dp)
                )
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, CardBorder)
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
                                Icon(Icons.Default.Person, contentDescription = null, tint = BrandPrimary)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = CardBorder,
                                focusedLabelColor = BrandPrimary
                            )
                        )

                        OutlinedTextField(
                            value = uiState.dosisActual,
                            onValueChange = { viewModel.onDosisActualChange(it) },
                            label = { Text("Dosis Asignada", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Medication, contentDescription = null, tint = BrandPrimary)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = CardBorder,
                                focusedLabelColor = BrandPrimary
                            )
                        )
                    }
                }
            }

            // ── Último Registro Generado (si existe) ──────────────────────────
            uiState.ultimoRegistro?.let { registro ->
                item {
                    SectionHeaderDispensador(
                        titulo = "Último Evento Registrado",
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 6.dp)
                    )
                }

                item {
                    val isSuccess = registro.pastillaDetectada
                    val statusColor = if (isSuccess) SuccessDark else ErrorRed
                    val badgeBg = if (isSuccess) SuccessSoft else ErrorSoft

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isSuccess) "Dosis Confirmada" else "Sin Detección",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                }
                                Surface(
                                    color = badgeBg,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = if (isSuccess) "OK" else "Alerta",
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Paciente: ${registro.paciente}", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text("Dosis: ${registro.dosis} · ${registro.dia}", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = SimpleDateFormat("HH:mm:ss · dd/MM/yyyy", Locale.getDefault()).format(Date(registro.timestamp)),
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
fun ResumenCardDispensador(
    label: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.85f))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = valor,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SectionHeaderDispensador(
    titulo: String,
    badge: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        if (badge != null) {
            Surface(
                color = PrimarySoft,
                shape = CircleShape
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary
                )
            }
        }
    }
}