package com.abdapps.ceireport.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.abdapps.ceireport.data.model.Report
import com.abdapps.ceireport.ui.components.SignaturePad
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
    val scrollState = rememberScrollState()

    // Activity Result Launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addPhotoToReport(context, it) }
    }

    // Camera launcher setup
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
        topBar = {
            TopAppBar(
                title = { Text(if (report.id == 0L) "Nuevo Reporte" else "Editar Reporte") },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fields
            OutlinedTextField(
                value = report.title,
                onValueChange = { viewModel.updateCurrentReport { r -> r.copy(title = it) } },
                label = { Text("Título del Reporte") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = report.date,
                    onValueChange = { viewModel.updateCurrentReport { r -> r.copy(date = it) } },
                    label = { Text("Fecha (dd/mm/aaaa)") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = report.location,
                    onValueChange = { viewModel.updateCurrentReport { r -> r.copy(location = it) } },
                    label = { Text("Ubicación") },
                    modifier = Modifier.weight(1.2f)
                )
            }

            OutlinedTextField(
                value = report.technicianName,
                onValueChange = { viewModel.updateCurrentReport { r -> r.copy(technicianName = it) } },
                label = { Text("Técnico Responsable") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = report.description,
                onValueChange = { viewModel.updateCurrentReport { r -> r.copy(description = it) } },
                label = { Text("Descripción de Actividades") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = report.observations,
                onValueChange = { viewModel.updateCurrentReport { r -> r.copy(observations = it) } },
                label = { Text("Observaciones") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Divider()

            // Photos Section
            Text("Evidencias Fotográficas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { launchCamera() },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cámara")
                }

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Galería")
                }
            }

            if (report.photos.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(report.photos) { path ->
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = File(path)),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            } else {
                Text(
                    "Ninguna fotografía adjuntada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Divider()

            // Signature Section
            Text("Firma del Técnico", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            
            Button(
                onClick = { showSignatureDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.Draw, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (report.signaturePath.isNullOrEmpty()) "Dibujar Firma" else "Modificar Firma")
            }

            if (!report.signaturePath.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = File(report.signaturePath)),
                        contentDescription = "Firma guardada",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Actions: Export & Share
            Button(
                onClick = {
                    if (report.title.isEmpty() || report.technicianName.isEmpty()) {
                        Toast.makeText(context, "Por favor complete el título y nombre del técnico", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.finalizeReport(context) { excelFile, pdfFile ->
                            Toast.makeText(context, "Reportes generados con éxito", Toast.LENGTH_SHORT).show()
                            shareFiles(context, excelFile, pdfFile)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generar y Compartir (Excel/PDF)", style = MaterialTheme.typography.titleMedium)
            }
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
}

private fun shareFiles(context: Context, excelFile: File, pdfFile: File) {
    try {
        val excelUri = FileProvider.getUriForFile(context, "com.abdapps.ceireport.fileprovider", excelFile)
        val pdfUri = FileProvider.getUriForFile(context, "com.abdapps.ceireport.fileprovider", pdfFile)

        val uris = arrayListOf<Uri>(excelUri, pdfUri)
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Reportes"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error al compartir archivos", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}
