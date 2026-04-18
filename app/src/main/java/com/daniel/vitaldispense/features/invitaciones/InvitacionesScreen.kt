package com.daniel.vitaldispense.features.invitaciones

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daniel.vitaldispense.data.model.Invitacion
import com.daniel.vitaldispense.features.auth.AuthResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitacionesScreen(
    viewModel: InvitacionesViewModel = viewModel()
) {
    val state by viewModel.invitacionesState.collectAsState()
    val envioState by viewModel.envioState.collectAsState()
    val context = LocalContext.current
    
    var showDialog by remember { mutableStateOf(false) }
    var emailInvitado by remember { mutableStateOf("") }

    // Escuchar el estado de envío para abrir el correo
    LaunchedEffect(envioState) {
        if (envioState is AuthResult.Success) {
            val email = (envioState as AuthResult.Success<String>).data
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, "Invitación a VitalDispense")
                putExtra(Intent.EXTRA_TEXT, "Hola, te invito a unirte a mi red de VitalDispense. Una vez que descargues la app, regístrate con este correo para acceder.")
            }
            try {
                context.startActivity(Intent.createChooser(intent, "Enviar correo con:"))
                viewModel.resetEnvioState()
                showDialog = false
                emailInvitado = ""
            } catch (e: Exception) {
                Toast.makeText(context, "No hay apps de correo instaladas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Gestión de Invitaciones") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Invitación")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state) {
                is AuthResult.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AuthResult.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error de conexión", color = MaterialTheme.colorScheme.error)
                        Text((state as AuthResult.Error).message, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.obtenerInvitacionesEnviadas() }) {
                            Text("Reintentar")
                        }
                    }
                }
                is AuthResult.Success -> {
                    val lista = (state as AuthResult.Success<List<Invitacion>>).data
                    if (lista.isEmpty()) {
                        EmptyState()
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(lista) { invitacion ->
                                InvitacionItem(invitacion)
                            }
                        }
                    }
                }
                else -> {}
            }

            // Diálogo para enviar invitación
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { if (envioState !is AuthResult.Loading) showDialog = false },
                    title = { Text("Nueva Invitación") },
                    text = {
                        Column {
                            Text("Ingresa el correo de la persona que deseas invitar.", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = emailInvitado,
                                onValueChange = { emailInvitado = it },
                                label = { Text("Correo electrónico") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.enviarInvitacion(emailInvitado) { /* Lanzado por LaunchedEffect */ } },
                            enabled = emailInvitado.isNotEmpty() && envioState !is AuthResult.Loading
                        ) {
                            if (envioState is AuthResult.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            } else {
                                Text("Guardar y Enviar")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }, enabled = envioState !is AuthResult.Loading) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun InvitacionItem(invitacion: Invitacion) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(invitacion.correo ?: "Sin correo", fontWeight = FontWeight.Bold)
                Text("Estado: ${invitacion.estado ?: "Pendiente"}", style = MaterialTheme.typography.bodySmall)
            }
            Badge(
                containerColor = if (invitacion.estado == "aceptada") Color(0xFF4CAF50) else Color(0xFFFFC107)
            ) {
                Text(invitacion.estado?.uppercase() ?: "P")
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No hay invitaciones enviadas", color = Color.Gray)
        Text("Usa el botón + para enviar la primera", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
    }
}
