package com.daniel.vitaldispense.features.hardware

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HardwareScreen(
    viewModel: HardwareViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val humedadActual by viewModel.humedadActual.collectAsState()
    val isDispensing by viewModel.isDispensing.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is HardwareUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is HardwareUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is HardwareUiState.Success -> {
                HardwareContent(
                    state = state,
                    humedadActual = humedadActual,
                    isDispensingGlobal = isDispensing, // Pasamos el flujo dinámico continuo
                    onDispenseClick = { viewModel.dispensarManualmente() }
                )
            }
        }
    }
}

@Composable
fun HardwareContent(
    state: HardwareUiState.Success,
    humedadActual: Int,
    isDispensingGlobal: Boolean,
    onDispenseClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // 1. Estado del Dispositivo IoT
        item {
            // CORRECCIÓN: Aquí inyectamos directamente 'isDispensingGlobal'
            // Esto asegura que el botón cambie a "Girando Motor..." y regrese a la normalidad al instante
            StatusCard(
                isOnline = state.isOnline,
                isDispensing = isDispensingGlobal,
                onDispenseClick = onDispenseClick
            )
        }

        // 2. Sensores en Tiempo Real
        item {
            SectionHeader(title = "Monitoreo Ambiental", icon = Icons.Default.Sensors)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SensorCard(
                    title = "Temperatura",
                    value = "${state.sensors.temperature}°C",
                    icon = Icons.Default.Thermostat,
                    color = Color(0xFFE57373),
                    modifier = Modifier.weight(1f)
                )
                SensorCard(
                    title = "Humedad",
                    value = "$humedadActual%",
                    icon = Icons.Default.WaterDrop,
                    color = if (humedadActual > 70) Color.Red else Color(0xFF64B5F6),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Gráfica de Humedad Dinámica
        item {
            HumidityLineChart(history = state.sensors.history, currentHumidity = humedadActual)
        }

        // 3. Inventario del Dispensador
        item {
            SectionHeader(title = "Estado del Inventario", icon = Icons.Default.Inventory)
        }

        items(state.inventario) { comp ->
            CompartimentoItem(comp)
        }
    }
}

@Composable
fun StatusCard(isOnline: Boolean, isDispensing: Boolean, onDispenseClick: () -> Unit) {
    val statusColor by animateColorAsState(
        targetValue = if (isOnline) Color(0xFF4CAF50) else Color.Gray,
        label = "statusColor"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOnline) "Dispensador en Línea" else "Dispensador Fuera de Línea",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOnline) Color(0xFF2E7D32) else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDispenseClick,
                enabled = isOnline && !isDispensing,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isDispensing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Girando Motor...")
                } else {
                    Icon(Icons.Default.Bolt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dispensar Dosis Manual")
                }
            }
        }
    }
}

@Composable
fun SensorCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun HumidityLineChart(history: List<Float>, currentHumidity: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Historial de Humedad (%)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (history.size < 2) return@Canvas
                val path = Path()
                val width = size.width
                val height = size.height
                val maxVal = 100f
                val spacing = width / (history.size - 1)

                history.forEachIndexed { index, value ->
                    val x = index * spacing
                    val y = height - (value / maxVal * height)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = if (currentHumidity > 70) Color.Red else Color(0xFF00897B),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun CompartimentoItem(comp: Compartimento) {
    val isLowStock = comp.cantidad <= comp.stockMinimo

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = comp.id.toString(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(comp.medicamento, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${comp.dosis} • En stock: ${comp.cantidad}", fontSize = 13.sp, color = Color.Gray)
            }
            if (isLowStock) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Stock Bajo",
                        color = Color.Red,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
    }
}