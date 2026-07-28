package com.abdapps.ceireport.data.repository

import com.abdapps.ceireport.data.local.ReportDao
import com.abdapps.ceireport.data.model.Report
import kotlinx.coroutines.flow.Flow

class ReportRepository(private val reportDao: ReportDao) {
    val allReports: Flow<List<Report>> = reportDao.getAllReports()

    suspend fun getReportById(id: Long): Report? {
        return reportDao.getReportById(id)
    }

    suspend fun insertReport(report: Report): Long {
        return reportDao.insertReport(report)
    }

    suspend fun updateReport(report: Report) {
        reportDao.updateReport(report)
    }

    suspend fun deleteReport(report: Report) {
        reportDao.deleteReport(report)
    }
}
