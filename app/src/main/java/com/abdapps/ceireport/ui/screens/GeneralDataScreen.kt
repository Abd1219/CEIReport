package com.abdapps.ceireport.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralDataScreen(
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit
) {
    val context = LocalContext.current
    val reportState by viewModel.currentReport.collectAsState()
    val report = reportState ?: return
    val scrollState = rememberScrollState()

    // ── DatePicker ────────────────────────────────────────────────────────────
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val fecha = "%02d/%02d/%04d".format(day, month + 1, year)
            viewModel.updateCurrentReport { r -> r.copy(date = fecha) }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Datos Generales",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Paso 1 de 3 — Identificación",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveDraft { onNavigateBack() }
                    }) {
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
                        onClick = { viewModel.saveDraft { onNavigateBack() } },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Guardar Borrador")
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
            StepProgressBar(currentStep = 1, totalSteps = 3)

            // ── Sección 1: Identificación del Proyecto ──────────────────────────
            FormCard(title = "Identificación del Proyecto") {
                StyledTextField(
                    value = report.proyecto,
                    onValueChange = { viewModel.updateCurrentReport { r -> r.copy(proyecto = it) } },
                    label = "Proyecto"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StyledTextField(
                        value = report.fase,
                        onValueChange = { viewModel.updateCurrentReport { r -> r.copy(fase = it) } },
                        label = "Fase",
                        modifier = Modifier.weight(1f)
                    )
                    StyledTextField(
                        value = report.area,
                        onValueChange = { viewModel.updateCurrentReport { r -> r.copy(area = it) } },
                        label = "Área",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StyledTextField(
                        value = report.sistema,
                        onValueChange = { viewModel.updateCurrentReport { r -> r.copy(sistema = it) } },
                        label = "Sistema",
                        modifier = Modifier.weight(1f)
                    )
                    StyledTextField(
                        value = report.disciplina,
                        onValueChange = { viewModel.updateCurrentReport { r -> r.copy(disciplina = it) } },
                        label = "Disciplina",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Sección 2: Responsable y Fecha ──────────────────────────────────
            FormCard(title = "Responsable y Fecha") {
                StyledTextField(
                    value = report.technicianName,
                    onValueChange = { viewModel.updateCurrentReport { r -> r.copy(technicianName = it) } },
                    label = "Nombre del Responsable / Inspector"
                )

                OutlinedTextField(
                    value = report.date,
                    onValueChange = { viewModel.updateCurrentReport { r -> r.copy(date = it) } },
                    label = { Text("Fecha del Reporte") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    readOnly = true,
                    colors = textFieldColors(),
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Seleccionar fecha",
                                tint = HeaderBlue
                            )
                        }
                    }
                )
            }

            // ── Sección 3: Contrato ─────────────────────────────────────────────
            FormCard(title = "Detalles del Contrato") {
                StyledTextField(
                    value = report.noContrato,
                    onValueChange = { viewModel.updateCurrentReport { r -> r.copy(noContrato = it) } },
                    label = "No. de Contrato"
                )

                OutlinedTextField(
                    value = report.descripcionAlcance,
                    onValueChange = { viewModel.updateCurrentReport { r -> r.copy(descripcionAlcance = it) } },
                    label = { Text("Descripción del Alcance de Contrato") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors(),
                    minLines = 3,
                    maxLines = 6
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── COMPONENTES REUTILIZABLES PARA FORMULARIOS ────────────────────────────────

@Composable
fun StepProgressBar(currentStep: Int, totalSteps: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Progreso del formulario",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            Text(
                text = "$currentStep de $totalSteps",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = HeaderBlue
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 1..totalSteps) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (i <= currentStep) HeaderBlue else Color(0xFFE2E8F0))
                )
            }
        }
    }
}

@Composable
fun FormCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = HeaderBlue
            )
            HorizontalDivider(color = Color(0xFFF1F5F9))
            content()
        }
    }
}

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = textFieldColors()
    )
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = HeaderBlue,
    unfocusedBorderColor = Color(0xFFCBD5E1),
    focusedLabelColor = HeaderBlue,
    unfocusedLabelColor = TextSecondary
)
