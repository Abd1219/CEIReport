package com.abdapps.ceireport.ui.screens

import androidx.compose.animation.*
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
import android.widget.Toast
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

    // ── Estado del diálogo de agregar actividad ───────────────────────────────
    var showAddDialog by remember { mutableStateOf(false) }
    var nuevaActividad by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Seguridad y Clima", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Pantalla 2 de 4",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveDraft { onNavigateBack() }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveDraft {
                            Toast.makeText(context, "Borrador guardado", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar Borrador")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { viewModel.saveDraft { onNavigateBack() } },
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Anterior")
                    }
                    Button(
                        onClick = { onNavigateNext() },
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text("Siguiente")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null,
                            modifier = Modifier.size(18.dp))
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
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ══════════════════════════════════════════════════════════════════
            // SECCIÓN 1: SEGURIDAD
            // ══════════════════════════════════════════════════════════════════
            SectionCard(
                icon = Icons.Default.Security,
                iconColor = Color(0xFF16A34A),
                title = "Actividades de Seguridad",
                subtitle = "Registra las acciones de seguridad realizadas en campo"
            ) {
                // Lista de actividades existentes
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
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }

                // Mensaje vacío
                AnimatedVisibility(visible = report.actividadesSeguridad.isEmpty()) {
                    Text(
                        text = "No hay actividades registradas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Botón agregar
                Button(
                    onClick = {
                        nuevaActividad = ""
                        showAddDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF16A34A)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar Actividad de Seguridad")
                }
            }

            // ══════════════════════════════════════════════════════════════════
            // SECCIÓN 2: CLIMA
            // ══════════════════════════════════════════════════════════════════
            SectionCard(
                icon = Icons.Default.WbSunny,
                iconColor = Color(0xFFF59E0B),
                title = "Condiciones Climáticas en Campo",
                subtitle = "Selecciona el clima durante la jornada (puedes elegir varios)"
            ) {
                // Grid de clima 3 columnas
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
                            // Rellena si la fila tiene menos de 3 elementos
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Resumen de selección
                AnimatedVisibility(visible = report.clima.isNotEmpty()) {
                    val seleccionados = CLIMAS
                        .filter { report.clima.contains(it.id) }
                        .joinToString("  ") { "${it.emoji} ${it.label.replace("\n", " ")}" }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Seleccionado: $seleccionados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // ── Diálogo: Agregar actividad de seguridad ───────────────────────────────
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
                    label = { Text("Describe la actividad") },
                    placeholder = { Text("Ej: Charla de 5 minutos sobre EPP") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    singleLine = false
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
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
}

// ── Componente: tarjeta de sección ────────────────────────────────────────────
@Composable
private fun SectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Encabezado de sección
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor,
                        modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(title, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
            HorizontalDivider()
            content()
        }
    }
}

// ── Componente: fila de actividad de seguridad ────────────────────────────────
@Composable
private fun ActividadItem(
    numero: Int,
    texto: String,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Número de ítem
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
            style = MaterialTheme.typography.bodyMedium
        )
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.DeleteOutline,
                contentDescription = "Eliminar",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Componente: tarjeta de condición climática ────────────────────────────────
@Composable
private fun ClimaCard(
    opcion: ClimaOpcion,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected)
        opcion.color.copy(alpha = 0.15f)
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    val borderColor = if (isSelected) opcion.color else Color.Transparent

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
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
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) opcion.color
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}
