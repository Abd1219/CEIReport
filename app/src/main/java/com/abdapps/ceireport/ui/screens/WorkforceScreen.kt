package com.abdapps.ceireport.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abdapps.ceireport.ui.theme.*
import com.abdapps.ceireport.ui.viewmodel.ReportViewModel

// ── Roles fijos de fuerza de trabajo ─────────────────────────────────────────
private val ROLES_TRABAJO = listOf(
    "Sp. Seg.",
    "Residente",
    "O.P.",
    "Topógrafo",
    "Cadenero",
    "Oficiales",
    "Ayudante",
    "Banderero",
    "Sup. Obra",
    "Sup. Calidad"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkforceScreen(
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit
) {
    val context = LocalContext.current
    val reportState by viewModel.currentReport.collectAsState()
    val report = reportState ?: return
    val scrollState = rememberScrollState()

    var showExitDialog by remember { mutableStateOf(false) }

    // Estado local para cantidades y horas — inicializado desde el reporte
    val cantidades = remember(report.id) {
        mutableStateListOf<String>().apply {
            ROLES_TRABAJO.indices.forEach { i ->
                add(report.fuerzaTrabajoCantidades.getOrElse(i) { "" })
            }
        }
    }
    val horas = remember(report.id) {
        mutableStateListOf<String>().apply {
            ROLES_TRABAJO.indices.forEach { i ->
                add(report.fuerzaTrabajoHoras.getOrElse(i) { "" })
            }
        }
    }

    // Totales calculados en tiempo real
    val totalCantidad = cantidades.sumOf { it.toIntOrNull() ?: 0 }
    val totalHoras = horas.sumOf { it.toDoubleOrNull() ?: 0.0 }

    // Función para persistir cambios al ViewModel
    fun persistChanges() {
        viewModel.updateCurrentReport { r ->
            r.copy(
                fuerzaTrabajoCantidades = cantidades.toList(),
                fuerzaTrabajoHoras = horas.toList()
            )
        }
    }

    // Interceptar botón atrás nativo del dispositivo
    BackHandler {
        persistChanges()
        viewModel.saveDraft { onNavigateBack() }
    }

    // Persistir automáticamente al desmontar la pantalla (navegación horizontal)
    DisposableEffect(Unit) {
        onDispose {
            persistChanges()
        }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Fuerza de Trabajo",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Paso 4 de 7 — Personal en Campo",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        persistChanges()
                        viewModel.saveDraft { onNavigateBack() }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        persistChanges()
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
            // Barra de progreso del flujo (Paso 4 de 8)
            StepProgressBar(
                currentStep = 4,
                totalSteps = 8,
                stepValids = viewModel.getStepValidations(report)
            )

            // ── TARJETA PRINCIPAL: FUERZA DE TRABAJO ─────────────────────────
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
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Encabezado de sección
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = HeaderBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Registro de Personal",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = HeaderBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Fila de encabezado de tabla ───────────────────────────
                    WorkforceTableHeader()

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = Color(0xFFE2E8F0),
                        thickness = 1.5.dp
                    )

                    // ── Filas de datos por rol ────────────────────────────────
                    ROLES_TRABAJO.forEachIndexed { index, rol ->
                        WorkforceTableRow(
                            rol = rol,
                            cantidad = cantidades.getOrElse(index) { "" },
                            horas = horas.getOrElse(index) { "" },
                            isAlternate = index % 2 == 1,
                            onCantidadChange = { value ->
                                if (index < cantidades.size) {
                                    cantidades[index] = value.filter { it.isDigit() }
                                    persistChanges()
                                }
                            },
                            onHorasChange = { value ->
                                if (index < horas.size) {
                                    horas[index] = value.filter { it.isDigit() || it == '.' }
                                    persistChanges()
                                }
                            }
                        )
                        if (index < ROLES_TRABAJO.lastIndex) {
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = HeaderBlue.copy(alpha = 0.3f),
                        thickness = 1.5.dp
                    )

                    // ── Fila de totales ───────────────────────────────────────
                    WorkforceTotalsRow(
                        totalCantidad = totalCantidad,
                        totalHoras = totalHoras
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Banner: Total de HH ───────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(HeaderBlue)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total de HH",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = formatHoras(totalHoras),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Diálogo de confirmación para salir/retroceder
    if (showExitDialog) {
        ExitConfirmationDialog(
            onDismiss = { showExitDialog = false },
            onSaveDraft = {
                persistChanges()
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

// ── COMPONENTES INTERNOS ───────────────────────────────────────────────────────

@Composable
private fun WorkforceTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(HeaderBlue.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Rol / Puesto",
            modifier = Modifier.weight(1.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = HeaderBlue
        )
        Text(
            text = "Cantidad",
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = HeaderBlue,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Horas",
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = HeaderBlue,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WorkforceTableRow(
    rol: String,
    cantidad: String,
    horas: String,
    isAlternate: Boolean,
    onCantidadChange: (String) -> Unit,
    onHorasChange: (String) -> Unit
) {
    val rowBg = if (isAlternate) Color(0xFFF8FAFC) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rol label
        Text(
            text = rol,
            modifier = Modifier
                .weight(1.8f)
                .padding(start = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        // Campo Cantidad
        OutlinedTextField(
            value = cantidad,
            onValueChange = onCantidadChange,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = HeaderBlue,
                unfocusedBorderColor = Color(0xFFCBD5E1)
            ),
            placeholder = {
                Text(
                    "0",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        )

        // Campo Horas
        OutlinedTextField(
            value = horas,
            onValueChange = onHorasChange,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = AccentOrange,
                unfocusedBorderColor = Color(0xFFCBD5E1)
            ),
            placeholder = {
                Text(
                    "0.0",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        )
    }
}

@Composable
private fun WorkforceTotalsRow(
    totalCantidad: Int,
    totalHoras: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AccentOrange.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "TOTAL",
            modifier = Modifier.weight(1.8f),
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AccentOrange
        )
        Text(
            text = "$totalCantidad",
            modifier = Modifier.weight(1f),
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AccentOrange,
            textAlign = TextAlign.Center
        )
        Text(
            text = formatHoras(totalHoras),
            modifier = Modifier.weight(1f),
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AccentOrange,
            textAlign = TextAlign.Center
        )
    }
}

/** Formatea las horas: sin decimales si son exactas, con 1 decimal si no */
private fun formatHoras(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        "${value.toLong()} hrs"
    } else {
        "%.1f hrs".format(value)
    }
}
