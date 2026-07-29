package com.abdapps.ceireport.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.GpsFixed
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

    var showExitDialog by remember { mutableStateOf(false) }

    // Launcher de Permiso de Ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            obtainLocation(context) { coords ->
                viewModel.updateCurrentReport { r -> r.copy(location = coords) }
            }
        } else {
            Toast.makeText(context, "Se requiere permiso de ubicación para obtener coordenadas", Toast.LENGTH_LONG).show()
        }
    }

    fun requestGpsLocation() {
        val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            obtainLocation(context) { coords ->
                viewModel.updateCurrentReport { r -> r.copy(location = coords) }
            }
        } else {
            locationPermissionLauncher.launch(permission)
        }
    }

    // Interceptar botón atrás nativo del dispositivo (Guarda borrador y regresa al menú principal)
    BackHandler {
        viewModel.saveDraft { onNavigateBack() }
    }

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
                            text = "Paso 1 de 7 — Identificación",
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
            StepProgressBar(
                currentStep = 1,
                totalSteps = 8,
                stepValids = viewModel.getStepValidations(report)
            )

            // ── Sección 1: Identificación del Proyecto ──────────────────────────
            FormCard(title = "Identificación del Proyecto") {
                StyledTextField(
                    value = report.proyecto,
                    onValueChange = { viewModel.updateCurrentReport { r -> r.copy(proyecto = it) } },
                    label = "Nombre del Proyecto"
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

            // ── Sección Ubicación / Coordenadas GPS ──────────────────────────────
            FormCard(title = "Ubicación y Coordenadas GPS") {
                OutlinedTextField(
                    value = report.location,
                    onValueChange = { viewModel.updateCurrentReport { r -> r.copy(location = it) } },
                    label = { Text("Ubicación / Coordenadas GPS") },
                    placeholder = { Text("Ej: Lat: 19.43260, Lng: -99.13320 o Sitio de obra...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { requestGpsLocation() }) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = "Obtener GPS",
                                tint = HeaderBlue
                            )
                        }
                    }
                )

                Button(
                    onClick = { requestGpsLocation() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HeaderBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.GpsFixed, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Obtener Coordenadas GPS Automáticas", fontWeight = FontWeight.Bold)
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

// ── COMPONENTES REUTILIZABLES PARA FORMULARIOS ────────────────────────────────

@Composable
fun ExitConfirmationDialog(
    onDismiss: () -> Unit,
    onSaveDraft: () -> Unit,
    onDiscard: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                tint = HeaderBlue,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = "¿Deseas guardar tu progreso?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Tienes cambios en este reporte. Puedes guardarlo como borrador para continuar más tarde o salir sin guardar.",
                fontSize = 14.sp,
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onSaveDraft,
                colors = ButtonDefaults.buttonColors(containerColor = HeaderBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar borrador")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDiscard) {
                    Text("Salir sin guardar", color = StatusPendingIcon)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}

@Composable
fun StepProgressBar(currentStep: Int, totalSteps: Int, stepValids: List<Boolean> = emptyList()) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Progreso del formulario",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$currentStep de $totalSteps",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 1..totalSteps) {
                val isCurrent = i == currentStep
                val isValid = stepValids.getOrNull(i - 1) ?: true
                
                val color = when {
                    isCurrent -> MaterialTheme.colorScheme.primary
                    i < currentStep && !isValid -> AccentOrange // Warning/Falta llenar
                    i < currentStep -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.outlineVariant
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
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
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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

/** Obtiene las coordenadas GPS actuales usando LocationManager */
@Suppress("DEPRECATION")
private fun obtainLocation(context: android.content.Context, onLocationObtained: (String) -> Unit) {
    try {
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
        if (locationManager == null) {
            Toast.makeText(context, "Servicio de ubicación no disponible", Toast.LENGTH_SHORT).show()
            return
        }
        val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            Toast.makeText(context, "Por favor activa el GPS en tu dispositivo", Toast.LENGTH_LONG).show()
            return
        }

        var lastLocation: android.location.Location? = null
        if (isGpsEnabled) {
            lastLocation = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
        }
        if (lastLocation == null && isNetworkEnabled) {
            lastLocation = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
        }

        if (lastLocation != null) {
            val coords = "Lat: %.5f, Lng: %.5f".format(java.util.Locale.US, lastLocation.latitude, lastLocation.longitude)
            onLocationObtained(coords)
            Toast.makeText(context, "Coordenadas GPS obtenidas", Toast.LENGTH_SHORT).show()
        } else {
            val listener = object : android.location.LocationListener {
                override fun onLocationChanged(location: android.location.Location) {
                    val coords = "Lat: %.5f, Lng: %.5f".format(java.util.Locale.US, location.latitude, location.longitude)
                    onLocationObtained(coords)
                    locationManager.removeUpdates(this)
                    Toast.makeText(context, "Coordenadas GPS obtenidas", Toast.LENGTH_SHORT).show()
                }
                override fun onProviderDisabled(provider: String) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
            }
            val provider = if (isGpsEnabled) android.location.LocationManager.GPS_PROVIDER else android.location.LocationManager.NETWORK_PROVIDER
            locationManager.requestSingleUpdate(provider, listener, null)
            Toast.makeText(context, "Obteniendo posición GPS...", Toast.LENGTH_SHORT).show()
        }
    } catch (e: SecurityException) {
        Toast.makeText(context, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error al obtener ubicación: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

