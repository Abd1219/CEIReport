package com.abdapps.ceireport.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abdapps.ceireport.data.excel.ExcelGenerator
import com.abdapps.ceireport.data.model.Report
import com.abdapps.ceireport.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ReportViewModel(private val repository: ReportRepository) : ViewModel() {

    val reportsList: StateFlow<List<Report>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentReport = MutableStateFlow<Report?>(null)
    val currentReport: StateFlow<Report?> = _currentReport.asStateFlow()

    fun getStepValidations(report: Report): List<Boolean> {
        return listOf(
            // Paso 1: Datos Generales (Proyecto no vacio y fecha no vacia)
            report.proyecto.isNotBlank() && report.date.isNotBlank(),
            // Paso 2: Seguridad y Clima (Tiene al menos un clima seleccionado o actividades)
            report.clima.isNotEmpty() || report.actividadesSeguridad.isNotEmpty(),
            // Paso 3: Actividades Realizadas
            report.actividadesRealizadas.isNotEmpty(),
            // Paso 4: Fuerza de Trabajo (Al menos un rol con cantidad > 0)
            report.fuerzaTrabajoCantidades.any { it.isNotBlank() && (it.toIntOrNull() ?: 0) > 0 },
            // Paso 5: Maquinaria Utilizada (Al menos un equipo con cantidad > 0)
            report.maquinariaCantidades.any { it.isNotBlank() && (it.toIntOrNull() ?: 0) > 0 },
            // Paso 6: Actividades Planeadas
            report.actividadesPlaneadas.isNotEmpty(),
            // Paso 7: Evidencias Fotográficas
            report.photos.isNotEmpty(),
            // Paso 8: Avance y Finalización (Al menos un responsable o supervisor)
            report.supervisor.isNotBlank() || report.technicianName.isNotBlank()
        )
    }

    fun createNewReport() {
        _currentReport.value = Report(
            date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        )
    }

    fun selectReport(report: Report) {
        _currentReport.value = report
    }

    fun updateCurrentReport(updateBlock: (Report) -> Report) {
        _currentReport.value = _currentReport.value?.let(updateBlock)
    }

    fun saveDraft(onComplete: () -> Unit = {}) {
        val report = _currentReport.value ?: return
        viewModelScope.launch {
            if (report.id == 0L) {
                val newId = repository.insertReport(report.copy(isDraft = true))
                _currentReport.value = report.copy(id = newId, isDraft = true)
            } else {
                repository.updateReport(report)
            }
            onComplete()
        }
    }

    fun deleteReport(report: Report) {
        viewModelScope.launch {
            repository.deleteReport(report)
        }
    }

    fun addPhotoToReport(context: Context, uri: Uri, caption: String = "") {
        val report = _currentReport.value ?: return
        viewModelScope.launch {
            try {
                // Copy selected/captured uri image to app storage
                val file = createTempImageFile(context, "photo")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                val updatedPhotos = report.photos.toMutableList().apply { add(file.absolutePath) }
                val updatedCaptions = report.photoCaptions.toMutableList().apply { add(caption) }
                updateCurrentReport { it.copy(photos = updatedPhotos, photoCaptions = updatedCaptions) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removePhotoAt(index: Int) {
        val report = _currentReport.value ?: return
        if (index in report.photos.indices) {
            val updatedPhotos = report.photos.toMutableList().apply { removeAt(index) }
            val updatedCaptions = report.photoCaptions.toMutableList().apply {
                if (index in indices) removeAt(index)
            }
            updateCurrentReport { it.copy(photos = updatedPhotos, photoCaptions = updatedCaptions) }
        }
    }

    fun updatePhotoCaption(index: Int, caption: String) {
        val report = _currentReport.value ?: return
        if (index in report.photos.indices) {
            val updatedCaptions = report.photoCaptions.toMutableList().apply {
                while (size <= index) add("")
                this[index] = caption
            }
            updateCurrentReport { it.copy(photoCaptions = updatedCaptions) }
        }
    }

    fun saveCroquis(context: Context, uri: Uri) {
        val report = _currentReport.value ?: return
        viewModelScope.launch {
            try {
                val file = createTempImageFile(context, "croquis")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                updateCurrentReport { it.copy(croquisPath = file.absolutePath) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeCroquis() {
        updateCurrentReport { it.copy(croquisPath = null) }
    }

    fun saveSignature(context: Context, bitmap: Bitmap) {
        val report = _currentReport.value ?: return
        viewModelScope.launch {
            try {
                val file = createTempImageFile(context, "signature", ".png")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                updateCurrentReport { it.copy(signaturePath = file.absolutePath) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeSignature() {
        updateCurrentReport { it.copy(signaturePath = null) }
    }

    fun saveSupervisorSignature(context: Context, bitmap: Bitmap) {
        val report = _currentReport.value ?: return
        viewModelScope.launch {
            try {
                val file = createTempImageFile(context, "sig_supervisor", ".png")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                updateCurrentReport { it.copy(supervisorSignaturePath = file.absolutePath) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeSupervisorSignature() {
        updateCurrentReport { it.copy(supervisorSignaturePath = null) }
    }

    fun finalizeReport(context: Context, onComplete: (excelFile: File) -> Unit) {
        val report = _currentReport.value ?: return
        viewModelScope.launch {
            val finalizedReport = report.copy(isDraft = false)
            if (finalizedReport.id == 0L) {
                val newId = repository.insertReport(finalizedReport)
                _currentReport.value = finalizedReport.copy(id = newId)
            } else {
                repository.updateReport(finalizedReport)
                _currentReport.value = finalizedReport
            }

            // Generar solo Excel
            val excelFile = ExcelGenerator.generate(context, _currentReport.value!!)
            onComplete(excelFile)
        }
    }

    private fun createTempImageFile(context: Context, prefix: String, extension: String = ".jpg"): File {
        val directory = File(context.getExternalFilesDir(null), "Media")
        if (!directory.exists()) directory.mkdirs()
        return File(directory, "${prefix}_${System.currentTimeMillis()}$extension")
    }
}

class ReportViewModelFactory(private val repository: ReportRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
