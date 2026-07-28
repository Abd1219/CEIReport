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

        // 3. Actividades Realizadas Block (lista dinámica)
        currentRow++
        val actHeaderRow = sheet.createRow(currentRow)
        actHeaderRow.heightInPoints = 22f
        val actHeaderCell = actHeaderRow.createCell(0)
        actHeaderCell.setCellValue("Actividades Realizadas")
        actHeaderCell.cellStyle = sectionHeaderStyle
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 0, 5))

        currentRow++
        if (report.actividadesRealizadas.isNotEmpty()) {
            report.actividadesRealizadas.forEachIndexed { index, actividad ->
                val actRow = sheet.createRow(currentRow)
                actRow.heightInPoints = 22f

                val numCell = actRow.createCell(0)
                numCell.setCellValue("${index + 1}.")
                numCell.cellStyle = labelStyle

                val actCell = actRow.createCell(1)
                actCell.setCellValue(actividad)
                actCell.cellStyle = valueStyle
                sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 1, 5))

                currentRow++
            }
        } else {
            val emptyRow = sheet.createRow(currentRow)
            emptyRow.heightInPoints = 20f
            val emptyCell = emptyRow.createCell(0)
            emptyCell.setCellValue("Sin actividades registradas")
            emptyCell.cellStyle = valueStyle
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 0, 5))
            currentRow++
        }

        // 4. Observaciones Block (lista dinámica)
        currentRow++
        val obsHeaderRow = sheet.createRow(currentRow)
        obsHeaderRow.heightInPoints = 22f
        val obsHeaderCell = obsHeaderRow.createCell(0)
        obsHeaderCell.setCellValue("Observaciones y Notas de Campo")
        obsHeaderCell.cellStyle = sectionHeaderStyle
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 0, 5))

        currentRow++
        if (report.observacionesList.isNotEmpty()) {
            report.observacionesList.forEachIndexed { index, obs ->
                val obsRow = sheet.createRow(currentRow)
                obsRow.heightInPoints = 22f

                val numCell = obsRow.createCell(0)
                numCell.setCellValue("${index + 1}.")
                numCell.cellStyle = labelStyle

                val obsCell = obsRow.createCell(1)
                obsCell.setCellValue(obs)
                obsCell.cellStyle = valueStyle
                sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 1, 5))

                currentRow++
            }
        } else {
            val emptyRow = sheet.createRow(currentRow)
            emptyRow.heightInPoints = 20f
            val emptyCell = emptyRow.createCell(0)
            emptyCell.setCellValue("Sin observaciones registradas")
            emptyCell.cellStyle = valueStyle
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 0, 5))
            currentRow++
        }

        currentRow++

        // 5. Fuerza de Trabajo Block
        val rolesNombres = listOf(
            "Sp. Seg.", "Residente", "O.P.", "Topógrafo", "Cadenero",
            "Oficiales", "Ayudante", "Banderero", "Sup. Obra", "Sup. Calidad"
        )

        val workforceHeaderRow = sheet.createRow(currentRow)
        workforceHeaderRow.heightInPoints = 22f
        val workforceHeaderCell = workforceHeaderRow.createCell(0)
        workforceHeaderCell.setCellValue("Fuerza de Trabajo")
        workforceHeaderCell.cellStyle = sectionHeaderStyle
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 0, 5))
        currentRow++

        // Sub-encabezado de columnas
        val wfColHeaderRow = sheet.createRow(currentRow)
        wfColHeaderRow.heightInPoints = 20f
        wfColHeaderRow.createCell(0).apply {
            setCellValue("Rol / Puesto")
            cellStyle = labelStyle
        }
        wfColHeaderRow.createCell(1).apply {
            setCellValue("Cantidad")
            cellStyle = labelStyle
        }
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 1, 2))
        wfColHeaderRow.createCell(3).apply {
            setCellValue("Horas")
            cellStyle = labelStyle
        }
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 3, 5))
        currentRow++

        // Filas de cada rol
        var totalCantidadWf = 0
        var totalHorasWf = 0.0
        rolesNombres.forEachIndexed { index, rol ->
            val wfRow = sheet.createRow(currentRow)
            wfRow.heightInPoints = 20f

            val cantStr = report.fuerzaTrabajoCantidades.getOrElse(index) { "" }
            val horStr = report.fuerzaTrabajoHoras.getOrElse(index) { "" }
            val cant = cantStr.toIntOrNull() ?: 0
            val hor = horStr.toDoubleOrNull() ?: 0.0
            totalCantidadWf += cant
            totalHorasWf += hor

            wfRow.createCell(0).apply { setCellValue(rol); cellStyle = labelStyle }
            wfRow.createCell(1).apply { setCellValue(cant.toDouble()); cellStyle = valueStyle }
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 1, 2))
            wfRow.createCell(3).apply { setCellValue(hor); cellStyle = valueStyle }
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 3, 5))
            currentRow++
        }

        // Fila de totales
        val wfTotalRow = sheet.createRow(currentRow)
        wfTotalRow.heightInPoints = 22f
        wfTotalRow.createCell(0).apply { setCellValue("TOTAL"); cellStyle = labelStyle }
        wfTotalRow.createCell(1).apply { setCellValue(totalCantidadWf.toDouble()); cellStyle = labelStyle }
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 1, 2))
        wfTotalRow.createCell(3).apply { setCellValue(totalHorasWf); cellStyle = labelStyle }
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 3, 5))
        currentRow++

        // Total HH
        val hhRow = sheet.createRow(currentRow)
        hhRow.heightInPoints = 24f
        hhRow.createCell(0).apply { setCellValue("Total de HH"); cellStyle = sectionHeaderStyle }
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 0, 2))
        hhRow.createCell(3).apply {
            setCellValue("${"%.1f".format(totalHorasWf)} hrs")
            cellStyle = sectionHeaderStyle
        }
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 3, 5))
        currentRow += 2

        // 6. Maquinaria Utilizada Block
        val equiposNombres = listOf(
            "Bailarina", "Hormigonera", "Minicar", "Vehículos",
            "Generador", "Rotomartillo", "Compresor"
        )

        val machineryHeaderRow = sheet.createRow(currentRow)
        machineryHeaderRow.heightInPoints = 22f
        val machineryHeaderCell = machineryHeaderRow.createCell(0)
        machineryHeaderCell.setCellValue("Maquinaria Utilizada")
        machineryHeaderCell.cellStyle = sectionHeaderStyle
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 0, 5))
        currentRow++

        // Sub-encabezado de columnas
        val macColHeaderRow = sheet.createRow(currentRow)
        macColHeaderRow.heightInPoints = 20f
        macColHeaderRow.createCell(0).apply {
            setCellValue("Maquinaria / Equipo")
            cellStyle = labelStyle
        }
        macColHeaderRow.createCell(1).apply {
            setCellValue("Cantidad")
            cellStyle = labelStyle
        }
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 1, 2))
        macColHeaderRow.createCell(3).apply {
            setCellValue("Horas")
            cellStyle = labelStyle
        }
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 3, 5))
        currentRow++

        // Filas de cada equipo
        var totalCantidadMac = 0
        var totalHorasMac = 0.0
        equiposNombres.forEachIndexed { index, equipo ->
            val macRow = sheet.createRow(currentRow)
            macRow.heightInPoints = 20f

            val cantStr = report.maquinariaCantidades.getOrElse(index) { "" }
            val horStr = report.maquinariaHoras.getOrElse(index) { "" }
            val cant = cantStr.toIntOrNull() ?: 0
            val hor = horStr.toDoubleOrNull() ?: 0.0
            totalCantidadMac += cant
            totalHorasMac += hor

            macRow.createCell(0).apply { setCellValue(equipo); cellStyle = labelStyle }
            macRow.createCell(1).apply { setCellValue(cant.toDouble()); cellStyle = valueStyle }
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 1, 2))
            macRow.createCell(3).apply { setCellValue(hor); cellStyle = valueStyle }
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 3, 5))
            currentRow++
        }

        // Fila de totales
        val macTotalRow = sheet.createRow(currentRow)
        macTotalRow.heightInPoints = 22f
        macTotalRow.createCell(0).apply { setCellValue("TOTAL"); cellStyle = labelStyle }
        macTotalRow.createCell(1).apply { setCellValue(totalCantidadMac.toDouble()); cellStyle = labelStyle }
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 1, 2))
        macTotalRow.createCell(3).apply { setCellValue(totalHorasMac); cellStyle = labelStyle }
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 3, 5))
        currentRow++

        // Total HM
        val hmRow = sheet.createRow(currentRow)
        hmRow.heightInPoints = 24f
        hmRow.createCell(0).apply { setCellValue("Total de HM"); cellStyle = sectionHeaderStyle }
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 0, 2))
        hmRow.createCell(3).apply {
            setCellValue("${"%.1f".format(totalHorasMac)} hrs")
            cellStyle = sectionHeaderStyle
        }
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 3, 5))
        currentRow += 2

        // 7. Actividades Planeadas Block
        currentRow++
        val planHeaderRow = sheet.createRow(currentRow)
        planHeaderRow.heightInPoints = 22f
        val planHeaderCell = planHeaderRow.createCell(0)
        planHeaderCell.setCellValue("Actividades Planeadas para el Siguiente Día")
        planHeaderCell.cellStyle = sectionHeaderStyle
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 0, 5))

        currentRow++
        if (report.actividadesPlaneadas.isNotEmpty()) {
            report.actividadesPlaneadas.forEachIndexed { index, actividad ->
                val planRow = sheet.createRow(currentRow)
                planRow.heightInPoints = 22f

                val numCell = planRow.createCell(0)
                numCell.setCellValue("${index + 1}.")
                numCell.cellStyle = labelStyle

                val actCell = planRow.createCell(1)
                actCell.setCellValue(actividad)
                actCell.cellStyle = valueStyle
                sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 1, 5))

                currentRow++
            }
        } else {
            val emptyRow = sheet.createRow(currentRow)
            emptyRow.heightInPoints = 20f
            val emptyCell = emptyRow.createCell(0)
            emptyCell.setCellValue("Sin actividades planeadas registradas")
            emptyCell.cellStyle = valueStyle
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 0, 5))
            currentRow++
        }

        currentRow++

        // Helper for Drawing Pictures
        val drawing = sheet.createDrawingPatriarch()

        // 8. Evidencias Fotográficas con Descripción
        if (report.photos.isNotEmpty()) {
            val photoHeaderRow = sheet.createRow(currentRow)
            photoHeaderRow.heightInPoints = 22f
            val photoHeaderCell = photoHeaderRow.createCell(0)
            photoHeaderCell.setCellValue("Evidencias Fotográficas")
            photoHeaderCell.cellStyle = sectionHeaderStyle
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 0, 5))

            currentRow += 2

            var photoCol = 0
            report.photos.forEachIndexed { index, photoPath ->
                val file = File(photoPath)
                val caption = report.photoCaptions.getOrElse(index) { "" }

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

                // Pie de foto debajo de la imagen
                if (caption.isNotBlank()) {
                    val capRow = sheet.getRow(currentRow + 8) ?: sheet.createRow(currentRow + 8)
                    capRow.heightInPoints = 18f
                    val capCell = capRow.createCell(photoCol)
                    capCell.setCellValue("Nota: $caption")
                    capCell.cellStyle = valueStyle
                    sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow + 8, currentRow + 8, photoCol, photoCol + 2))
                }

                photoCol += 3
                if (photoCol >= 6) {
                    photoCol = 0
                    currentRow += 10
                }
            }
            if (photoCol != 0) {
                currentRow += 10
            }
            currentRow++
        }

        // 9. Croquis Descriptivo Section
        if (!report.croquisPath.isNullOrEmpty()) {
            val croquisFile = File(report.croquisPath)
            if (croquisFile.exists()) {
                val croquisHeaderRow = sheet.createRow(currentRow)
                croquisHeaderRow.heightInPoints = 22f
                val croquisHeaderCell = croquisHeaderRow.createCell(0)
                croquisHeaderCell.setCellValue("Croquis Descriptivo")
                croquisHeaderCell.cellStyle = sectionHeaderStyle
                sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(currentRow, currentRow, 0, 5))

                currentRow += 2

                try {
                    val bytes = compressImage(croquisFile, 600, 400)
                    if (bytes != null) {
                        val pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG)
                        val anchor = workbook.creationHelper.createClientAnchor().apply {
                            setCol1(0)
                            setRow1(currentRow)
                            setCol2(6)
                            setRow2(currentRow + 12)
                        }
                        drawing.createPicture(anchor, pictureIdx)
                        currentRow += 13
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                currentRow++
            }
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
