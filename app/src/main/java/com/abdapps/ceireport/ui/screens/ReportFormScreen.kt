package com.abdapps.ceireport.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
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

    // Estado para añadir o editar pie de foto
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showCaptionDialog by remember { mutableStateOf(false) }
    var photoCaptionInput by remember { mutableStateOf("") }
    var editingCaptionIndex by remember { mutableStateOf<Int?>(null) }

    // Estado para captura de Croquis Descriptivo
    var isCroquisPicker by remember { mutableStateOf(false) }

    // Interceptar botón atrás nativo del dispositivo (Guarda borrador y regresa al menú principal)
    BackHandler {
        viewModel.saveDraft { onNavigateBack() }
    }

    // ── Launchers de Cámara y Galería con Manejo Seguro de Permisos ──────────
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher de Cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            val uri = tempCameraUri!!
            if (isCroquisPicker) {
                viewModel.saveCroquis(context, uri)
                isCroquisPicker = false
            } else {
                pendingPhotoUri = uri
                photoCaptionInput = ""
                showCaptionDialog = true
            }
        }
    }

    // Launcher de Permiso de Cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCameraInternal(context) { uri ->
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, "Se requiere permiso de cámara para tomar fotos", Toast.LENGTH_LONG).show()
        }
    }

    fun requestCameraAccess(forCroquis: Boolean = false) {
        isCroquisPicker = forCroquis
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            launchCameraInternal(context) { uri ->
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Launcher de Galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (isCroquisPicker) {
                viewModel.saveCroquis(context, it)
                isCroquisPicker = false
            } else {
                pendingPhotoUri = it
                photoCaptionInput = ""
                showCaptionDialog = true
            }
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
                            text = "Paso 7 de 8 — Evidencias y Firma",
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
            // Barra de progreso del flujo (Paso 7 de 8)
            StepProgressBar(
                currentStep = 7,
                totalSteps = 8,
                stepValids = viewModel.getStepValidations(report)
            )

            // ── SECCIÓN 1: EVIDENCIAS FOTOGRÁFICAS ──────────────────────────
            FormCard(title = "Evidencias Fotográficas") {
                Text(
                    text = "Adjunta fotografías tomadas en campo con su respectiva descripción para incluir en el reporte Excel:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { requestCameraAccess(forCroquis = false) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = HeaderBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cámara")
                    }

                    OutlinedButton(
                        onClick = {
                            isCroquisPicker = false
                            galleryLauncher.launch("image/*")
                        },
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
                            .height(180.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(report.photos) { index, path ->
                            val caption = report.photoCaptions.getOrElse(index) { "" }
                            Card(
                                modifier = Modifier
                                    .width(150.dp)
                                    .fillMaxHeight(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(115.dp)
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
                                                .clickable { viewModel.removePhotoAt(index) },
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
                                    // Pie de foto / Descripción
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                            .clickable {
                                                editingCaptionIndex = index
                                                photoCaptionInput = caption
                                            },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (caption.isNotBlank()) caption else "+ Agregar nota",
                                            fontSize = 11.sp,
                                            color = if (caption.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar nota",
                                            tint = HeaderBlue,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
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
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se han adjuntado fotos aún",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── SECCIÓN 2: CROQUIS DESCRIPTIVO ──────────────────────────────
            FormCard(title = "Croquis Descriptivo") {
                Text(
                    text = "Adjunta una imagen del croquis o esquema descriptivo del área de trabajo:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!report.croquisPath.isNullOrEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = File(report.croquisPath)),
                                    contentDescription = "Croquis Descriptivo",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .clickable { viewModel.removeCroquis() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Eliminar croquis",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { requestCameraAccess(forCroquis = true) }) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Cambiar Foto", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { requestCameraAccess(forCroquis = true) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = HeaderBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cámara Croquis")
                        }

                        OutlinedButton(
                            onClick = {
                                isCroquisPicker = true
                                galleryLauncher.launch("image/*")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HeaderBlue)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Galería Croquis")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ── DIÁLOGO PARA INGRESAR / EDITAR DESCRIPCIÓN DE FOTO ───────────────────
    if (showCaptionDialog && pendingPhotoUri != null) {
        AlertDialog(
            onDismissRequest = {
                pendingPhotoUri?.let { viewModel.addPhotoToReport(context, it, "") }
                showCaptionDialog = false
                pendingPhotoUri = null
            },
            title = {
                Text("Descripción de la Fotografía", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Añade una breve nota o descripción de la imagen capturada:", fontSize = 13.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = photoCaptionInput,
                        onValueChange = { photoCaptionInput = it },
                        label = { Text("Pie de foto (opcional)") },
                        placeholder = { Text("Ej: Avance de colado en flete norte...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingPhotoUri?.let { uri ->
                            viewModel.addPhotoToReport(context, uri, photoCaptionInput.trim())
                        }
                        showCaptionDialog = false
                        pendingPhotoUri = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HeaderBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Guardar Foto")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingPhotoUri?.let { uri ->
                        viewModel.addPhotoToReport(context, uri, "")
                    }
                    showCaptionDialog = false
                    pendingPhotoUri = null
                }) {
                    Text("Omitir Nota", color = TextSecondary)
                }
            }
        )
    }

    // ── DIÁLOGO PARA EDITAR DESCRIPCIÓN EXISTENTE ─────────────────────────────
    if (editingCaptionIndex != null) {
        val index = editingCaptionIndex!!
        AlertDialog(
            onDismissRequest = { editingCaptionIndex = null },
            title = { Text("Editar Descripción", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = photoCaptionInput,
                    onValueChange = { photoCaptionInput = it },
                    label = { Text("Pie de foto") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 4
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updatePhotoCaption(index, photoCaptionInput.trim())
                        editingCaptionIndex = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HeaderBlue)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCaptionIndex = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
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

// ── FUNCIÓN INTERNA PARA PREPARAR FILE PROVIDER URI CON SEGURIDAD ───────────────
private fun launchCameraInternal(context: Context, onUriReady: (Uri) -> Unit) {
    try {
        val directory = File(context.getExternalFilesDir(null), "Media")
        if (!directory.exists()) directory.mkdirs()
        val file = File(directory, "cam_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "com.abdapps.ceireport.fileprovider", file)
        onUriReady(uri)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al crear archivo de cámara: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

fun shareExcel(context: Context, excelFile: File) {
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
