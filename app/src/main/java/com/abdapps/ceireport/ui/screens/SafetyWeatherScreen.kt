package com.abdapps.ceireport.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

// ── Tipos de clima disponibles ────────────────────────────────────────────────
data class ClimaOpcion(
    val id: String,
    val emoji: String,
    val label: String,
    val color: Color
)

private val CLIMAS = listOf(
    ClimaOpcion("soleado",        "☀️",  "Soleado",             Color(0xFFF59E0B)),
    ClimaOpcion("parcial",        "⛅",  "Parcialmente\nNublado", Color(0xFF60A5FA)),
    ClimaOpcion("nublado",        "☁️",  "Nublado",             Color(0xFF94A3B8)),
    ClimaOpcion("lluvioso",       "🌧️",  "Lluvioso",            Color(0xFF3B82F6)),
    ClimaOpcion("tormenta",       "⛈️",  "Tormenta\nEléctrica", Color(0xFF7C3AED)),
    ClimaOpcion("neblina",        "🌫️",  "Neblina",             Color(0xFF9CA3AF)),
    ClimaOpcion("ventoso",        "💨",  "Ventoso",             Color(0xFF06B6D4)),
    ClimaOpcion("caluroso",       "🌡️",  "Caluroso",            Color(0xFFEF4444)),
    ClimaOpcion("frio",           "❄️",  "Frío",                Color(0xFF93C5FD)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyWeatherScreen(
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit
) {
    val context = LocalContext.current
    val reportState by viewModel.currentReport.collectAsState()
    val report = reportState ?: return
    val scrollState = rememberScrollState()

    var showAddDialog by remember { mutableStateOf(false) }
    var nuevaActividad by remember { mutableStateOf("") }
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
                            text = "Seguridad y Clima",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Paso 2 de 3 — Condiciones de Campo",
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
            // Barra de progreso del flujo
            StepProgressBar(currentStep = 2, totalSteps = 3)

            // ── SECCIÓN 1: SEGURIDAD ─────────────────────────────────────────
            FormCard(title = "Actividades de Seguridad") {
                AnimatedVisibility(visible = report.actividadesSeguridad.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        report.actividadesSeguridad.forEachIndexed { index, actividad ->
                            ActividadItem(
                                numero = index + 1,
                                texto = actividad,
                                onDelete = {
                                    val updated = report.actividadesSeguridad
                                        .toMutableList()
                                        .also { it.removeAt(index) }
                                    viewModel.updateCurrentReport { r ->
                                        r.copy(actividadesSeguridad = updated)
                                    }
                                }
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = report.actividadesSeguridad.isEmpty()) {
                    Text(
                        text = "No hay actividades de seguridad registradas aún.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Button(
                    onClick = {
                        nuevaActividad = ""
                        showAddDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar Actividad de Seguridad", fontWeight = FontWeight.Bold)
                }
            }

            // ── SECCIÓN 2: CLIMA ─────────────────────────────────────────────
            FormCard(title = "Condiciones Climáticas en Campo") {
                Text(
                    text = "Selecciona las condiciones observadas durante la jornada:",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                val rows = CLIMAS.chunked(3)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { opcion ->
                                val isSelected = report.clima.contains(opcion.id)
                                ClimaCard(
                                    opcion = opcion,
                                    isSelected = isSelected,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        val updated = report.clima.toMutableList().apply {
                                            if (isSelected) remove(opcion.id) else add(opcion.id)
                                        }
                                        viewModel.updateCurrentReport { r ->
                                            r.copy(clima = updated)
                                        }
                                    }
                                )
                            }
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = report.clima.isNotEmpty()) {
                    val seleccionados = CLIMAS
                        .filter { report.clima.contains(it.id) }
                        .joinToString("  ") { "${it.emoji} ${it.label.replace("\n", " ")}" }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = HeaderBlue.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "Clima seleccionado: $seleccionados",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = HeaderBlue,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Diálogo: Agregar actividad de seguridad
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            icon = {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text("Nueva Actividad de Seguridad") },
            text = {
                OutlinedTextField(
                    value = nuevaActividad,
                    onValueChange = { nuevaActividad = it },
                    label = { Text("Descripción de la actividad") },
                    placeholder = { Text("Ej: Plática de 5 min sobre EPP") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors(),
                    minLines = 2,
                    maxLines = 4
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val texto = nuevaActividad.trim()
                        if (texto.isNotEmpty()) {
                            val updated = report.actividadesSeguridad
                                .toMutableList()
                                .also { it.add(texto) }
                            viewModel.updateCurrentReport { r ->
                                r.copy(actividadesSeguridad = updated)
                            }
                        }
                        showAddDialog = false
                        nuevaActividad = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
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
private fun ActividadItem(
    numero: Int,
    texto: String,
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
                .background(Color(0xFF16A34A)),
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

@Composable
private fun ClimaCard(
    opcion: ClimaOpcion,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) opcion.color.copy(alpha = 0.15f) else Color(0xFFF8FAFC)
    val borderColor = if (isSelected) opcion.color else Color(0xFFE2E8F0)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = opcion.emoji, fontSize = 28.sp)
            Text(
                text = opcion.label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) opcion.color else TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}
