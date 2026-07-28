package com.abdapps.ceireport.data.excel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.abdapps.ceireport.data.model.Report
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object ExcelGenerator {

    fun generate(context: Context, report: Report): File {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Reporte Diario")

        // Enable gridlines
        sheet.isDisplayGridlines = true

        // Colors
        val headerColor = IndexedColors.DARK_BLUE.index
        val textColor = IndexedColors.BLACK.index
        val lightGray = IndexedColors.GREY_25_PERCENT.index

        // Fonts
        val titleFont = workbook.createFont().apply {
            fontName = "Arial"
            fontHeightInPoints = 16
            bold = true
            color = IndexedColors.WHITE.index
        }

        val sectionFont = workbook.createFont().apply {
            fontName = "Arial"
            fontHeightInPoints = 12
            bold = true
            color = headerColor
        }

        val boldFont = workbook.createFont().apply {
            fontName = "Arial"
            fontHeightInPoints = 10
            bold = true
        }

        val normalFont = workbook.createFont().apply {
            fontName = "Arial"
            fontHeightInPoints = 10
        }

        // Styles
        val titleStyle = workbook.createCellStyle().apply {
            fillForegroundColor = headerColor
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
            setFont(titleFont)
        }

        val labelStyle = workbook.createCellStyle().apply {
            setFont(boldFont)
            fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }

        val valueStyle = workbook.createCellStyle().apply {
            setFont(normalFont)
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            wrapText = true
        }

        val sectionHeaderStyle = workbook.createCellStyle().apply {
            setFont(sectionFont)
            borderBottom = BorderStyle.MEDIUM
            bottomBorderColor = headerColor
        }

        // 1. Title Banner
        val titleRow = sheet.createRow(0)
        titleRow.heightInPoints = 40f
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("REPORTE DIARIO CEI")
        titleCell.cellStyle = titleStyle
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5))

        // 2. Metadata Info Block
        val fields = listOf(
            "Título:" to report.title,
            "Fecha:" to report.date,
            "Técnico:" to report.technicianName,
            "Ubicación:" to report.location
        )

        var currentRow = 2
        for ((label, value) in fields) {
            val row = sheet.createRow(currentRow)
            row.heightInPoints = 20f

            val cellLabel = row.createCell(0)
            cellLabel.setCellValue(label)
            cellLabel.cellStyle = labelStyle

            val cellValue = row.createCell(1)
            cellValue.setCellValue(value)
            cellValue.cellStyle = valueStyle
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 1, 5))

            currentRow++
        }

        // 3. Description Block
        currentRow++
        val descHeaderRow = sheet.createRow(currentRow)
        descHeaderRow.heightInPoints = 22f
        val descHeaderCell = descHeaderRow.createCell(0)
        descHeaderCell.setCellValue("Actividades Realizadas / Descripción")
        descHeaderCell.cellStyle = sectionHeaderStyle
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 0, 5))

        currentRow++
        val descRow = sheet.createRow(currentRow)
        descRow.heightInPoints = 80f
        val descCell = descRow.createCell(0)
        descCell.setCellValue(report.description)
        descCell.cellStyle = valueStyle
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow + 2, 0, 5))

        currentRow += 4

        // 4. Observations Block
        val obsHeaderRow = sheet.createRow(currentRow)
        obsHeaderRow.heightInPoints = 22f
        val obsHeaderCell = obsHeaderRow.createCell(0)
        obsHeaderCell.setCellValue("Observaciones")
        obsHeaderCell.cellStyle = sectionHeaderStyle
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 0, 5))

        currentRow++
        val obsRow = sheet.createRow(currentRow)
        obsRow.heightInPoints = 50f
        val obsCell = obsRow.createCell(0)
        obsCell.setCellValue(report.observations)
        obsCell.cellStyle = valueStyle
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow + 1, 0, 5))

        currentRow += 3

        // Helper for Drawing Pictures
        val drawing = sheet.createDrawingPatriarch()

        // 5. Photos Section
        if (report.photos.isNotEmpty()) {
            val photoHeaderRow = sheet.createRow(currentRow)
            photoHeaderRow.heightInPoints = 22f
            val photoHeaderCell = photoHeaderRow.createCell(0)
            photoHeaderCell.setCellValue("Evidencias Fotográficas")
            photoHeaderCell.cellStyle = sectionHeaderStyle
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 0, 5))

            currentRow += 2

            var photoCol = 0
            for (photoPath in report.photos) {
                val file = File(photoPath)
                if (file.exists()) {
                    try {
                        val bytes = compressImage(file, 400, 300)
                        if (bytes != null) {
                            val pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG)
                            val anchor = workbook.creationHelper.createClientAnchor().apply {
                                setCol1(photoCol)
                                setRow1(currentRow)
                                setCol2(photoCol + 3)
                                setRow2(currentRow + 8)
                            }
                            drawing.createPicture(anchor, pictureIdx)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                photoCol += 3
                if (photoCol >= 6) {
                    photoCol = 0
                    currentRow += 9
                }
            }
            if (photoCol != 0) {
                currentRow += 9
            }
            currentRow++
        }

        // 6. Signature Section
        if (!report.signaturePath.isNullOrEmpty()) {
            val sigFile = File(report.signaturePath)
            if (sigFile.exists()) {
                val sigHeaderRow = sheet.createRow(currentRow)
                sigHeaderRow.heightInPoints = 22f
                val sigHeaderCell = sigHeaderRow.createCell(0)
                sigHeaderCell.setCellValue("Firma del Técnico")
                sigHeaderCell.cellStyle = sectionHeaderStyle
                sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 0, 5))

                currentRow += 2

                try {
                    val bytes = compressImage(sigFile, 200, 100)
                    if (bytes != null) {
                        val pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG)
                        val anchor = workbook.creationHelper.createClientAnchor().apply {
                            setCol1(1)
                            setRow1(currentRow)
                            setCol2(4)
                            setRow2(currentRow + 4)
                        }
                        drawing.createPicture(anchor, pictureIdx)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                currentRow += 5
            }
        }

        // Set column widths
        sheet.setColumnWidth(0, 4500)
        for (i in 1..5) {
            sheet.setColumnWidth(i, 4000)
        }

        // Output file
        val outputDir = File(context.getExternalFilesDir(null), "Reports")
        if (!outputDir.exists()) outputDir.mkdirs()
        val file = File(outputDir, "Reporte_${report.id}_${System.currentTimeMillis()}.xlsx")
        FileOutputStream(file).use { out ->
            workbook.write(out)
        }
        workbook.close()

        return file
    }

    private fun compressImage(file: File, width: Int, height: Int): ByteArray? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.absolutePath, options)
        
        var inSampleSize = 1
        if (options.outHeight > height || options.outWidth > width) {
            val halfHeight = options.outHeight / 2
            val halfWidth = options.outWidth / 2
            while ((halfHeight / inSampleSize) >= height && (halfWidth / inSampleSize) >= width) {
                inSampleSize *= 2
            }
        }

        options.inJustDecodeBounds = false
        options.inSampleSize = inSampleSize
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val bytes = stream.toByteArray()
        bitmap.recycle()
        return bytes
    }
}
