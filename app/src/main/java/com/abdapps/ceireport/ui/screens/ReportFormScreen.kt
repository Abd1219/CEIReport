package com.abdapps.ceireport.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.abdapps.ceireport.ui.components.SignaturePad
import com.abdapps.ceireport.ui.theme.*
import com.abdapps.ceireport.ui.viewmodel.ReportViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormScreen(
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val reportState by viewModel.currentReport.collectAsState()
    val report = reportState ?: return

    var showSignatureDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Interceptar botón atrás nativo del dispositivo
    BackHandler {
        showExitDialog = true
    }

    // Activity Result Launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addPhotoToReport(context, it) }
    }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            tempCameraUri?.let { viewModel.addPhotoToReport(context, it) }
        }
    }

    fun launchCamera() {
        try {
            val directory = File(context.getExternalFilesDir(null), "Media")
            if (!directory.exists()) directory.mkdirs()
            val file = File(directory, "cam_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "com.abdapps.ceireport.fileprovider", file)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Error al abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (report.id == 0L) "Nuevo Reporte" else "Editar Reporte",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Paso 5 de 5 — Evidencias y Firma",
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
                        onClick = {
                            if (report.proyecto.isEmpty() && report.title.isEmpty()) {
                                Toast.makeText(context, "Por favor indica el nombre o proyecto", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.finalizeReport(context) { excelFile ->
                                    Toast.makeText(context, "Reporte Excel generado con éxito", Toast.LENGTH_SHORT).show()
                                    shareExcel(context, excelFile)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Finalizar y Compartir Excel", fontWeight = FontWeight.Bold)
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
            StepProgressBar(currentStep = 5, totalSteps = 5)

            // ── SECCIÓN 1: EVIDENCIAS FOTOGRÁFICAS ──────────────────────────
            FormCard(title = "Evidencias Fotográficas") {
                Text(
                    text = "Adjunta fotografías tomadas en campo para incluir en el reporte Excel:",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { launchCamera() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = HeaderBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cámara")
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HeaderBlue)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Galería")
                    }
                }

                if (report.photos.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(report.photos) { path ->
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = File(path)),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Botón eliminar foto
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .clickable {
                                            val updated = report.photos.toMutableList().apply { remove(path) }
                                            viewModel.updateCurrentReport { r -> r.copy(photos = updated) }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Eliminar",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se han adjuntado fotos aún",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // ── SECCIÓN 3: FIRMA DIGITAL ─────────────────────────────────────
            FormCard(title = "Firma del Responsable") {
                Button(
                    onClick = { showSignatureDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = HeaderBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Draw, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (report.signaturePath.isNullOrEmpty()) "Dibujar Firma Digital" else "Modificar Firma",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!report.signaturePath.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = File(report.signaturePath)),
                            contentDescription = "Firma guardada",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showSignatureDialog) {
        Dialog(onDismissRequest = { showSignatureDialog = false }) {
            SignaturePad(
                onSignatureSaved = { bitmap ->
                    viewModel.saveSignature(context, bitmap)
                    showSignatureDialog = false
                },
                onDismiss = { showSignatureDialog = false }
            )
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

private fun shareExcel(context: Context, excelFile: File) {
    try {
        val excelUri = FileProvider.getUriForFile(context, "com.abdapps.ceireport.fileprovider", excelFile)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, excelUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Reporte Excel"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error al compartir el archivo Excel", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}
