package com.abdapps.ceireport.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.abdapps.ceireport.ui.components.SignaturePad
import com.abdapps.ceireport.ui.theme.*
import com.abdapps.ceireport.ui.viewmodel.ReportViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalizeScreen(
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val reportState by viewModel.currentReport.collectAsState()
    val report = reportState ?: return
    val scrollState = rememberScrollState()

    // ── Estado local de áreas/avances ─────────────────────────────────────────
    val areas = remember(report.id) {
        androidx.compose.runtime.snapshots.SnapshotStateList<String>().apply {
            addAll(report.areasAvance.ifEmpty { listOf("") })
        }
    }
    val porcentajes = remember(report.id) {
        androidx.compose.runtime.snapshots.SnapshotStateList<String>().apply {
            addAll(report.avancePorcentajes.ifEmpty { listOf("") })
        }
    }

    // Sincronizar listas (siempre del mismo tamaño)
    fun syncLists() {
        while (porcentajes.size < areas.size) porcentajes.add("")
        while (areas.size < porcentajes.size) areas.add("")
    }

    // Supervisor y Responsable de Contratista
    var supervisor by remember(report.id) { mutableStateOf(report.supervisor) }
    var responsable by remember(report.id) { mutableStateOf(report.technicianName) }

    fun saveProgressAndExit() {
        syncLists()
        viewModel.updateCurrentReport { r ->
            r.copy(
                areasAvance = areas.toList(),
                avancePorcentajes = porcentajes.toList(),
                supervisor = supervisor,
                technicianName = responsable
            )
        }
        viewModel.saveDraft { onNavigateBack() }
    }

    BackHandler { saveProgressAndExit() }

    // Diálogos de firma
    var showSupervisorSigDialog by remember { mutableStateOf(false) }
    var showContractorSigDialog by remember { mutableStateOf(false) }

    fun persistAndFinalize(onDone: (File) -> Unit) {
        syncLists()
        viewModel.updateCurrentReport { r ->
            r.copy(
                areasAvance = areas.toList(),
                avancePorcentajes = porcentajes.toList(),
                supervisor = supervisor,
                technicianName = responsable
            )
        }
        viewModel.finalizeReport(context) { excelFile ->
            onDone(excelFile)
        }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Avance y Finalización",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Paso 8 de 8 — Resumen Final",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { saveProgressAndExit() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        syncLists()
                        viewModel.updateCurrentReport { r ->
                            r.copy(
                                areasAvance = areas.toList(),
                                avancePorcentajes = porcentajes.toList(),
                                supervisor = supervisor,
                                technicianName = responsable
                            )
                        }
                        viewModel.saveDraft {
                            Toast.makeText(context, "Borrador guardado", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar Borrador", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderBlue)
            )
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
            // Barra de progreso del flujo (Paso 8 de 8)
            StepProgressBar(
                currentStep = 8,
                totalSteps = 8,
                stepValids = viewModel.getStepValidations(report)
            )

            // ── SECCIÓN 1: AVANCE POR ÁREA ─────────────────────────────────────
            FormCard(title = "Avance por Área / Disciplina") {
                Text(
                    text = "Registra el porcentaje de avance actual en cada área o disciplina involucrada:",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filas dinámicas de área + porcentaje
                areas.forEachIndexed { index, area ->
                    AreaAvanceRow(
                        index = index,
                        area = area,
                        porcentaje = porcentajes.getOrElse(index) { "" },
                        onAreaChange = { areas[index] = it },
                        onPorcentajeChange = { raw ->
                            val clean = raw.filter { it.isDigit() }.take(3)
                            val value = clean.toIntOrNull()
                            if (value == null || value <= 100) {
                                while (porcentajes.size <= index) porcentajes.add("")
                                porcentajes[index] = clean
                            }
                        },
                        onRemove = if (areas.size > 1) ({
                            areas.removeAt(index)
                            if (index < porcentajes.size) porcentajes.removeAt(index)
                        }) else null
                    )

                    if (index < areas.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Color(0xFFE0E6F0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón agregar área
                OutlinedButton(
                    onClick = {
                        areas.add("")
                        porcentajes.add("")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HeaderBlue)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Agregar Área", fontWeight = FontWeight.SemiBold)
                }

                // Tarjetas de avance visual
                if (areas.any { it.isNotBlank() }) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Resumen de Avance",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    areas.forEachIndexed { index, area ->
                        if (area.isNotBlank()) {
                            val pct = porcentajes.getOrElse(index) { "0" }.toIntOrNull()?.coerceIn(0, 100) ?: 0
                            AreaProgressCard(areaName = area, percentage = pct)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            // ── SECCIÓN 2: RESPONSABLES Y FIRMAS ─────────────────────────────
            FormCard(title = "Responsables y Firmas") {
                // ── Supervisor ────────────────────────────────────────────────
                OutlinedTextField(
                    value = supervisor,
                    onValueChange = { supervisor = it },
                    label = { Text("Supervisor") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = HeaderBlue)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HeaderBlue,
                        focusedLabelColor = HeaderBlue
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Firma Supervisor
                val supervisorSigFile = report.supervisorSignaturePath?.let { File(it) }
                if (supervisorSigFile != null && supervisorSigFile.exists()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Firma del Supervisor", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HeaderBlue)
                                TextButton(onClick = { viewModel.removeSupervisorSignature() }) {
                                    Text("Cambiar", fontSize = 12.sp, color = Color(0xFFC62828))
                                }
                            }
                            Image(
                                painter = rememberAsyncImagePainter(supervisorSigFile),
                                contentDescription = "Firma del Supervisor",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFD0D7DE), RoundedCornerShape(8.dp))
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showSupervisorSigDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HeaderBlue)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Firmar Supervisor", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE0E6F0))
                Spacer(modifier = Modifier.height(16.dp))

                // ── Responsable de Contratista ─────────────────────────────────
                OutlinedTextField(
                    value = responsable,
                    onValueChange = { responsable = it },
                    label = { Text("Responsable de Contratista") },
                    leadingIcon = {
                        Icon(Icons.Default.Engineering, contentDescription = null, tint = AccentOrange)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentOrange,
                        focusedLabelColor = AccentOrange
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Firma Contratista
                val contractorSigFile = report.signaturePath?.let { File(it) }
                if (contractorSigFile != null && contractorSigFile.exists()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Firma del Contratista", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AccentOrange)
                                TextButton(onClick = { viewModel.removeSignature() }) {
                                    Text("Cambiar", fontSize = 12.sp, color = Color(0xFFC62828))
                                }
                            }
                            Image(
                                painter = rememberAsyncImagePainter(contractorSigFile),
                                contentDescription = "Firma del Contratista",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFD0D7DE), RoundedCornerShape(8.dp))
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showContractorSigDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentOrange)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Firmar Contratista", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ── Popups para Firmas ──────────────────────────────────────────────
            if (showSupervisorSigDialog) {
                Dialog(onDismissRequest = { showSupervisorSigDialog = false }) {
                    SignaturePad(
                        onSignatureSaved = { bitmap ->
                            viewModel.saveSupervisorSignature(context, bitmap)
                            showSupervisorSigDialog = false
                        },
                        onDismiss = { showSupervisorSigDialog = false }
                    )
                }
            }

            if (showContractorSigDialog) {
                Dialog(onDismissRequest = { showContractorSigDialog = false }) {
                    SignaturePad(
                        onSignatureSaved = { bitmap ->
                            viewModel.saveSignature(context, bitmap)
                            showContractorSigDialog = false
                        },
                        onDismiss = { showContractorSigDialog = false }
                    )
                }
            }

            // ── BOTÓN FINALIZAR ────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (supervisor.isBlank() && responsable.isBlank()) {
                        Toast.makeText(context, "Por favor indica al menos un responsable", Toast.LENGTH_SHORT).show()
                    } else {
                        persistAndFinalize { excelFile ->
                            Toast.makeText(context, "Reporte Excel generado con éxito", Toast.LENGTH_SHORT).show()
                            shareExcel(context, excelFile)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Finalizar y Compartir Excel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Componente: Fila de Área + Porcentaje ──────────────────────────────────────
@Composable
private fun AreaAvanceRow(
    index: Int,
    area: String,
    porcentaje: String,
    onAreaChange: (String) -> Unit,
    onPorcentajeChange: (String) -> Unit,
    onRemove: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = area,
            onValueChange = onAreaChange,
            label = { Text("Área ${index + 1}", fontSize = 12.sp) },
            modifier = Modifier.weight(1.6f),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HeaderBlue,
                focusedLabelColor = HeaderBlue
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )

        OutlinedTextField(
            value = porcentaje,
            onValueChange = onPorcentajeChange,
            label = { Text("%", fontSize = 12.sp) },
            modifier = Modifier.weight(0.8f),
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            suffix = { Text("%", fontSize = 12.sp, color = TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HeaderBlue,
                focusedLabelColor = HeaderBlue
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )

        if (onRemove != null) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(36.dp))
        }
    }
}

// ── Componente: Tarjeta de Avance Visual ─────────────────────────────────────
@Composable
private fun AreaProgressCard(areaName: String, percentage: Int) {
    val animatedProgress by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(durationMillis = 600),
        label = "avance_$areaName"
    )

    val progressColor = when {
        percentage >= 80 -> Color(0xFF2E7D32)   // Verde
        percentage >= 50 -> Color(0xFFF57F17)   // Amarillo/Naranja
        else             -> Color(0xFFC62828)   // Rojo
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FF)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = areaName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(progressColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$percentage%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                }
            }
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.15f)
            )
        }
    }
}
