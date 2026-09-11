package com.daniel.vitaldispense.features.pacientes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Modelo del formulario ─────────────────────────────────────────────────────

data class FormPaciente(
    // Paso 1 — Datos demográficos
    var apellidos: String = "",
    var nombre: String = "",
    var fechaNacimiento: String = "",
    var sexo: String = "",
    var peso: String = "",
    var talla: String = "",

    // Paso 2 — Ubicación
    var habitacion: String = "",
    var cama: String = "",
    var idDispensador: String = "",
    var medicoTratante: String = "",

    // Paso 3 — Información clínica
    var alergias: String = "",
    var diagnostico: String = "",
    var tieneAlergia: Boolean = false,
    var viaAdministracion: String = "Oral",

    // Paso 4 — Medicación
    var medicamento: String = "",
    var dosis: String = "",
    var frecuencia: String = "",
    var compartimento: String = ""
)

// ── Pantalla principal ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarPacienteScreen(
    onBackClick: () -> Unit,
    onPacienteGuardado: () -> Unit
) {
    var pasoActual by remember { mutableIntStateOf(0) }
    var form by remember { mutableStateOf(FormPaciente()) }

    val pasos = listOf(
        "Datos personales",
        "Ubicación",
        "Info clínica",
        "Medicación"
    )

    Scaffold(
        containerColor = Color(0xFFF5F6FA),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nuevo Paciente",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (pasoActual > 0) pasoActual-- else onBackClick()
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color(0xFF1976D2)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F6FA)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Indicador de pasos ────────────────────────────────────────────
            StepperIndicator(
                pasos = pasos,
                pasoActual = pasoActual,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp),
                color = Color(0xFFE0E0E0)
            )

            // ── Contenido del paso ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                when (pasoActual) {
                    0 -> PasoDatosDemograficos(form) { form = it }
                    1 -> PasoUbicacion(form) { form = it }
                    2 -> PasoInfoClinica(form) { form = it }
                    3 -> PasoMedicacion(form) { form = it }
                }
            }

            // ── Botones de navegación ─────────────────────────────────────────
            HorizontalDivider(color = Color(0xFFE0E0E0))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (pasoActual > 0) {
                    OutlinedButton(
                        onClick = { pasoActual-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Anterior")
                    }
                }

                Button(
                    onClick = {
                        if (pasoActual < pasos.size - 1) {
                            pasoActual++
                        } else {
                            onPacienteGuardado()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pasoActual == pasos.size - 1)
                            Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (pasoActual == pasos.size - 1) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Paciente", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Siguiente", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

// ── Indicador de pasos ────────────────────────────────────────────────────────

@Composable
fun StepperIndicator(pasos: List<String>, pasoActual: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        pasos.forEachIndexed { index, nombre ->
            val completado = index < pasoActual
            val activo = index == pasoActual

            // Círculo del paso
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            completado -> Color(0xFF2E7D32)
                            activo     -> Color(0xFF1976D2)
                            else       -> Color(0xFFE0E0E0)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (completado) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = (index + 1).toString(),
                        color = if (activo) Color.White else Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Línea conectora (excepto después del último)
            if (index < pasos.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (completado) Color(0xFF2E7D32) else Color(0xFFE0E0E0)
                        )
                )
            }
        }
    }

    // Etiqueta del paso actual
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Paso ${pasoActual + 1} de ${pasos.size} · ${pasos[pasoActual]}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF1976D2),
            fontWeight = FontWeight.Medium
        )
    }
}

// ── PASO 1: Datos demográficos ────────────────────────────────────────────────

@Composable
fun PasoDatosDemograficos(form: FormPaciente, onUpdate: (FormPaciente) -> Unit) {
    val sexoOpciones = listOf("Masculino", "Femenino", "Otro")

    StepTitle(titulo = "Información demográfica", descripcion = "Datos de identificación del paciente")

    FormField(label = "Apellidos *") {
        CampoTexto(
            value = form.apellidos,
            placeholder = "Ej. García López",
            onValueChange = { onUpdate(form.copy(apellidos = it)) }
        )
    }

    FormField(label = "Nombre(s) *") {
        CampoTexto(
            value = form.nombre,
            placeholder = "Ej. María Fernanda",
            onValueChange = { onUpdate(form.copy(nombre = it)) }
        )
    }

    FormField(label = "Fecha de nacimiento *") {
        CampoTexto(
            value = form.fechaNacimiento,
            placeholder = "DD/MM/AAAA",
            onValueChange = { onUpdate(form.copy(fechaNacimiento = it)) },
            keyboardType = KeyboardType.Number
        )
    }

    FormField(label = "Sexo biológico *") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            sexoOpciones.forEach { opcion ->
                val seleccionado = form.sexo == opcion
                FilterChip(
                    selected = seleccionado,
                    onClick = { onUpdate(form.copy(sexo = opcion)) },
                    label = { Text(opcion, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FormField(label = "Peso (kg)", modifier = Modifier.weight(1f)) {
            CampoTexto(
                value = form.peso,
                placeholder = "Ej. 70",
                onValueChange = { onUpdate(form.copy(peso = it)) },
                keyboardType = KeyboardType.Decimal
            )
        }
        FormField(label = "Talla (cm)", modifier = Modifier.weight(1f)) {
            CampoTexto(
                value = form.talla,
                placeholder = "Ej. 165",
                onValueChange = { onUpdate(form.copy(talla = it)) },
                keyboardType = KeyboardType.Number
            )
        }
    }
}

// ── PASO 2: Ubicación ─────────────────────────────────────────────────────────

@Composable
fun PasoUbicacion(form: FormPaciente, onUpdate: (FormPaciente) -> Unit) {
    val habitaciones = listOf("Hab. 10", "Hab. 11", "Hab. 12", "Hab. 13", "Hab. 14")
    val camas = listOf("101", "102", "103", "104", "105", "106", "107", "108",
                       "109", "110", "111", "112", "113", "114", "115")

    StepTitle(titulo = "Ubicación y asignación", descripcion = "Cama e identificador del dispensador IoT")

    FormField(label = "Habitación *") {
        MenuDesplegable(
            opciones = habitaciones,
            seleccionado = form.habitacion,
            placeholder = "Seleccionar habitación",
            onSeleccionar = { onUpdate(form.copy(habitacion = it)) }
        )
    }

    FormField(label = "Número de cama *") {
        MenuDesplegable(
            opciones = camas,
            seleccionado = form.cama,
            placeholder = "Seleccionar cama",
            onSeleccionar = { onUpdate(form.copy(cama = it)) }
        )
    }

    FormField(label = "ID del Dispensador IoT *") {
        CampoTexto(
            value = form.idDispensador,
            placeholder = "Ej. VD-001, VD-002…",
            onValueChange = { onUpdate(form.copy(idDispensador = it)) }
        )
    }

    InfoCard(
        mensaje = "El ID del dispensador se encuentra en la etiqueta del hardware o en el panel de administración."
    )

    FormField(label = "Médico tratante") {
        CampoTexto(
            value = form.medicoTratante,
            placeholder = "Ej. Dr. Rodríguez Vázquez",
            onValueChange = { onUpdate(form.copy(medicoTratante = it)) }
        )
    }
}

// ── PASO 3: Información clínica ───────────────────────────────────────────────

@Composable
fun PasoInfoClinica(form: FormPaciente, onUpdate: (FormPaciente) -> Unit) {
    val vias = listOf("Oral", "Sonda nasogástrica", "Sonda PEG", "Sublingual")

    StepTitle(titulo = "Información clínica", descripcion = "Datos médicos relevantes para la medicación")

    // Alergias — destacado visualmente
    FormField(label = "¿El paciente tiene alergias conocidas?") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Switch(
                checked = form.tieneAlergia,
                onCheckedChange = { onUpdate(form.copy(tieneAlergia = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFFD32F2F)
                )
            )
            Text(
                text = if (form.tieneAlergia) "Sí, especificar abajo" else "No",
                fontWeight = FontWeight.Medium,
                color = if (form.tieneAlergia) Color(0xFFD32F2F) else Color.Gray
            )
        }
    }

    if (form.tieneAlergia) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(20.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedTextField(
                    value = form.alergias,
                    onValueChange = { onUpdate(form.copy(alergias = it)) },
                    placeholder = { Text("Ej. Penicilina, AINEs, látex…") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F),
                        unfocusedBorderColor = Color(0xFFEF9A9A),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }
        }
    }

    FormField(label = "Diagnóstico principal *") {
        OutlinedTextField(
            value = form.diagnostico,
            onValueChange = { onUpdate(form.copy(diagnostico = it)) },
            placeholder = { Text("Razón de internamiento…") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }

    FormField(label = "Vía de administración *") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            vias.forEach { via ->
                val seleccionado = form.viaAdministracion == via
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (seleccionado) Color(0xFFF0F7FF) else Color.White
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = seleccionado,
                        onClick = { onUpdate(form.copy(viaAdministracion = via)) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        via,
                        fontSize = 14.sp,
                        fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (seleccionado) Color(0xFF1976D2) else Color(0xFF1A1C1E)
                    )
                }
            }
        }
    }
}

// ── PASO 4: Esquema de medicación ─────────────────────────────────────────────

@Composable
fun PasoMedicacion(form: FormPaciente, onUpdate: (FormPaciente) -> Unit) {
    val frecuencias = listOf(
        "Cada 4 horas", "Cada 6 horas", "Cada 8 horas",
        "Cada 12 horas", "Una vez al día", "Dos veces al día"
    )
    val compartimentos = listOf("Tolva 1", "Tolva 2", "Tolva 3", "Tolva 4",
                                 "Bandeja A", "Bandeja B", "Bandeja C", "Bandeja D")

    StepTitle(titulo = "Esquema de medicación", descripcion = "Programa el dispensador para este paciente")

    FormField(label = "Medicamento *") {
        CampoTexto(
            value = form.medicamento,
            placeholder = "Ej. Paracetamol 500mg",
            onValueChange = { onUpdate(form.copy(medicamento = it)) }
        )
    }

    FormField(label = "Dosis *") {
        CampoTexto(
            value = form.dosis,
            placeholder = "Ej. 1 tableta, 2 cápsulas…",
            onValueChange = { onUpdate(form.copy(dosis = it)) }
        )
    }

    FormField(label = "Frecuencia *") {
        MenuDesplegable(
            opciones = frecuencias,
            seleccionado = form.frecuencia,
            placeholder = "Seleccionar frecuencia",
            onSeleccionar = { onUpdate(form.copy(frecuencia = it)) }
        )
    }

    FormField(label = "Compartimento del dispensador *") {
        MenuDesplegable(
            opciones = compartimentos,
            seleccionado = form.compartimento,
            placeholder = "Seleccionar tolva/bandeja",
            onSeleccionar = { onUpdate(form.copy(compartimento = it)) }
        )
    }

    InfoCard(
        mensaje = "El compartimento debe coincidir con la tolva física del dispensador asignado a la cama ${form.cama.ifBlank { "—" }}."
    )

    // Resumen del paciente antes de guardar
    if (form.nombre.isNotBlank() || form.apellidos.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Resumen del registro",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                ResumenFila("Paciente", "${form.apellidos} ${form.nombre}".trim().ifBlank { "—" })
                ResumenFila("Cama", form.cama.ifBlank { "—" })
                ResumenFila("Dispensador", form.idDispensador.ifBlank { "—" })
                ResumenFila("Medicamento", form.medicamento.ifBlank { "—" })
                ResumenFila("Frecuencia", form.frecuencia.ifBlank { "—" })
                if (form.tieneAlergia && form.alergias.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Alergias: ${form.alergias}",
                            fontSize = 12.sp,
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ── Componentes de apoyo del formulario ───────────────────────────────────────

@Composable
fun StepTitle(titulo: String, descripcion: String) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(titulo, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1A1C1E))
        Text(descripcion, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun FormField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.padding(bottom = 16.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF455A64),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        content()
    }
}

@Composable
fun CampoTexto(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.LightGray, fontSize = 14.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.LightGray,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuDesplegable(
    opciones: List<String>,
    seleccionado: String,
    placeholder: String,
    onSeleccionar: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it }
    ) {
        OutlinedTextField(
            value = seleccionado,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(placeholder, color = Color.LightGray, fontSize = 14.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccionar(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

@Composable
fun InfoCard(mensaje: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Text(
            text = "ℹ $mensaje",
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFF57F17)
        )
    }
}

@Composable
fun ResumenFila(label: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(valor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1C1E))
    }
}
