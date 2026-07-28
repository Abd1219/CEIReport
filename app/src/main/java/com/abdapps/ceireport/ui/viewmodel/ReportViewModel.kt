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

    fun addPhotoToReport(context: Context, uri: Uri) {
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
                updateCurrentReport { it.copy(photos = updatedPhotos) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
