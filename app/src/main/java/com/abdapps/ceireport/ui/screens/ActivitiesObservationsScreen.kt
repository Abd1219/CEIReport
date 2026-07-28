package com.abdapps.ceireport.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abdapps.ceireport.ui.theme.*
import com.abdapps.ceireport.ui.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesObservationsScreen(
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit
) {
    val context = LocalContext.current
    val reportState by viewModel.currentReport.collectAsState()
    val report = reportState ?: return
    val scrollState = rememberScrollState()

    var showAddActivityDialog by remember { mutableStateOf(false) }
    var nuevaActividad by remember { mutableStateOf("") }

    var showAddObservationDialog by remember { mutableStateOf(false) }
    var nuevaObservacion by remember { mutableStateOf("") }

    var showExitDialog by remember { mutableStateOf(false) }

    // Interceptar botón atrás nativo del dispositivo
    BackHandler {
        showExitDialog = true
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Actividades y Observaciones",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Paso 3 de 7 — Registro de Trabajo",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveDraft {
                            Toast.makeText(context, "Borrador guardado", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar Borrador", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeaderBlue
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onNavigateBack() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Anterior")
                    }

                    Button(
                        onClick = { onNavigateNext() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text("Siguiente", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Barra de progreso del flujo (Paso 3 de 5)
            StepProgressBar(currentStep = 3, totalSteps = 7)

            // ── SECCIÓN 1: ACTIVIDADES REALIZADAS ───────────────────────────
            FormCard(title = "Actividades Realizadas") {
                Text(
                    text = "Registra las tareas y actividades ejecutadas durante la jornada:",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                AnimatedVisibility(visible = report.actividadesRealizadas.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        report.actividadesRealizadas.forEachIndexed { index, actividad ->
                            ItemRegistroCard(
                                numero = index + 1,
                                texto = actividad,
                                badgeColor = HeaderBlue,
                                onDelete = {
                                    val updated = report.actividadesRealizadas
                                        .toMutableList()
                                        .also { it.removeAt(index) }
                                    viewModel.updateCurrentReport { r ->
                                        r.copy(actividadesRealizadas = updated)
                                    }
                                }
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = report.actividadesRealizadas.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se han agregado actividades aún",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                Button(
                    onClick = {
                        nuevaActividad = ""
                        showAddActivityDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HeaderBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar Actividad Realizada", fontWeight = FontWeight.Bold)
                }
            }

            // ── SECCIÓN 2: OBSERVACIONES / NOTAS DE CAMPO ───────────────────
            FormCard(title = "Observaciones y Notas de Campo") {
                Text(
                    text = "Añade notas, hallazgos o comentarios relevantes de supervisión:",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                AnimatedVisibility(visible = report.observacionesList.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        report.observacionesList.forEachIndexed { index, observacion ->
                            ItemRegistroCard(
                                numero = index + 1,
                                texto = observacion,
                                badgeColor = AccentOrange,
                                onDelete = {
                                    val updated = report.observacionesList
                                        .toMutableList()
                                        .also { it.removeAt(index) }
                                    viewModel.updateCurrentReport { r ->
                                        r.copy(observacionesList = updated)
                                    }
                                }
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = report.observacionesList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se han agregado observaciones aún",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                Button(
                    onClick = {
                        nuevaObservacion = ""
                        showAddObservationDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar Observación", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Diálogo: Agregar Actividad Realizada
    if (showAddActivityDialog) {
        AlertDialog(
            onDismissRequest = { showAddActivityDialog = false },
            icon = {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = null,
                    tint = HeaderBlue,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text("Nueva Actividad Realizada") },
            text = {
                OutlinedTextField(
                    value = nuevaActividad,
                    onValueChange = { nuevaActividad = it },
                    label = { Text("Descripción de la actividad") },
                    placeholder = { Text("Ej: Montaje e inspección de estructura metálica en Eje C-4") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors(),
                    minLines = 3,
                    maxLines = 5
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val texto = nuevaActividad.trim()
                        if (texto.isNotEmpty()) {
                            val updated = report.actividadesRealizadas
                                .toMutableList()
                                .also { it.add(texto) }
                            viewModel.updateCurrentReport { r ->
                                r.copy(actividadesRealizadas = updated)
                            }
                        }
                        showAddActivityDialog = false
                        nuevaActividad = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HeaderBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddActivityDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo: Agregar Observación
    if (showAddObservationDialog) {
        AlertDialog(
            onDismissRequest = { showAddObservationDialog = false },
            icon = {
                Icon(
                    Icons.Default.Comment,
                    contentDescription = null,
                    tint = AccentOrange,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text("Nueva Observación") },
            text = {
                OutlinedTextField(
                    value = nuevaObservacion,
                    onValueChange = { nuevaObservacion = it },
                    label = { Text("Detalle de la observación") },
                    placeholder = { Text("Ej: Retraso de 30 min por entrega diferida de material") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors(),
                    minLines = 3,
                    maxLines = 5
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val texto = nuevaObservacion.trim()
                        if (texto.isNotEmpty()) {
                            val updated = report.observacionesList
                                .toMutableList()
                                .also { it.add(texto) }
                            viewModel.updateCurrentReport { r ->
                                r.copy(observacionesList = updated)
                            }
                        }
                        showAddObservationDialog = false
                        nuevaObservacion = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddObservationDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmación para salir/retroceder
    if (showExitDialog) {
        ExitConfirmationDialog(
            onDismiss = { showExitDialog = false },
            onSaveDraft = {
                viewModel.saveDraft {
                    showExitDialog = false
                    onNavigateBack()
                }
            },
            onDiscard = {
                showExitDialog = false
                onNavigateBack()
            }
        )
    }
}

@Composable
private fun ItemRegistroCard(
    numero: Int,
    texto: String,
    badgeColor: Color,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$numero",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = texto,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = TextPrimary
        )
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Eliminar",
                tint = StatusPendingIcon,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
