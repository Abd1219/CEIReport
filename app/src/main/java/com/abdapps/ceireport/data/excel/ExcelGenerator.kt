package com.abdapps.ceireport.data.excel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.abdapps.ceireport.data.model.Report
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelGenerator {

    // ── Corporate Colors ───────────────────────────────────────────────────────
    private fun rgb(r: Int, g: Int, b: Int) =
        XSSFColor(byteArrayOf(r.toByte(), g.toByte(), b.toByte()), null)

    private val cBlueDark   = rgb(26,  60,  110)   // #1A3C6E — Encabezado principal
    private val cBlueMid    = rgb(46,  95,  163)   // #2E5FA3 — Bandas de sección
    private val cBlueLight  = rgb(214, 228, 247)   // #D6E4F7 — Fondo de etiquetas
    private val cBluePale   = rgb(235, 244, 255)   // #EBF4FF — Zebra par
    private val cOrange     = rgb(232, 119, 34)    // #E87722 — Totales / acento CEI
    private val cWhite      = rgb(255, 255, 255)
    private val cBlack      = rgb(10,  10,  10)
    private val cGrayText   = rgb(120, 120, 120)   // Pie de página

    fun generate(context: Context, report: Report): File {
        val workbook = XSSFWorkbook()
        val sheet    = workbook.createSheet("Reporte Diario")

        // Sin gridlines para look más limpio
        sheet.isDisplayGridlines = false

        // ── Helpers de creación de fuentes y estilos ───────────────────────────
        fun makeFont(
            name:  String   = "Calibri",
            size:  Short    = 10,
            bold:  Boolean  = false,
            color: XSSFColor = cBlack
        ): XSSFFont = (workbook.createFont() as XSSFFont).apply {
            fontName            = name
            fontHeightInPoints  = size
            this.bold           = bold
            setColor(color)
        }

        fun makeStyle(
            bg:          XSSFColor?           = null,
            font:        XSSFFont,
            hAlign:      HorizontalAlignment  = HorizontalAlignment.LEFT,
            vAlign:      VerticalAlignment    = VerticalAlignment.CENTER,
            wrap:        Boolean              = false,
            border:      BorderStyle          = BorderStyle.NONE,
            borderColor: XSSFColor            = cBlueLight
        ): XSSFCellStyle = (workbook.createCellStyle() as XSSFCellStyle).apply {
            setFont(font)
            alignment         = hAlign
            verticalAlignment = vAlign
            wrapText          = wrap
            bg?.let {
                setFillForegroundColor(it)
                fillPattern = FillPatternType.SOLID_FOREGROUND
            }
            if (border != BorderStyle.NONE) {
                borderTop    = border; setTopBorderColor(borderColor)
                borderBottom = border; setBottomBorderColor(borderColor)
                borderLeft   = border; setLeftBorderColor(borderColor)
                borderRight  = border; setRightBorderColor(borderColor)
            }
        }

        // ── Fuentes ────────────────────────────────────────────────────────────
        val fTitle      = makeFont("Calibri", 18, true,  cWhite)
        val fSubtitle   = makeFont("Calibri", 10, false, cWhite)
        val fSection    = makeFont("Calibri", 11, true,  cWhite)
        val fLabelWhite = makeFont("Calibri", 10, true,  cWhite)
        val fLabelBlue  = makeFont("Calibri", 10, true,  cBlueDark)
        val fValue      = makeFont("Calibri", 10, false, cBlack)
        val fTotal      = makeFont("Calibri", 11, true,  cWhite)
        val fFooter     = makeFont("Calibri",  9, false, cGrayText)
        val fCargo      = makeFont("Calibri",  9, false, cGrayText)

        // ── Estilos ────────────────────────────────────────────────────────────

        // Encabezado principal (fondo azul oscuro)
        val stHeader    = makeStyle(cBlueDark, fTitle, HorizontalAlignment.LEFT,   VerticalAlignment.CENTER, true)
        val stHeaderFill= makeStyle(cBlueDark, fSubtitle)
        val stSubBar    = makeStyle(cBlueDark, fSubtitle, HorizontalAlignment.RIGHT)

        // Separador naranja (sin texto, solo color)
        val stOrangeBar = (workbook.createCellStyle() as XSSFCellStyle).apply {
            setFillForegroundColor(cOrange); fillPattern = FillPatternType.SOLID_FOREGROUND
        }

        // Encabezados de sección (banda azul media)
        val stSection   = makeStyle(cBlueMid, fSection,    HorizontalAlignment.LEFT, VerticalAlignment.CENTER)

        // Etiquetas (fondo azul claro, texto azul oscuro negrita)
        val stLabel     = makeStyle(cBlueLight, fLabelBlue, HorizontalAlignment.LEFT, VerticalAlignment.CENTER,
                                    false, BorderStyle.THIN, cBlueMid)

        // Etiqueta de columna en tabla (fondo azul medio, texto blanco negrita)
        val stColHeader = makeStyle(cBlueMid, fLabelWhite, HorizontalAlignment.CENTER, VerticalAlignment.CENTER,
                                    false, BorderStyle.THIN, cBlueDark)

        // Valores — dos variantes: normal (blanco) y zebra (azul pálido)
        val stValue     = makeStyle(null,      fValue, HorizontalAlignment.LEFT,   VerticalAlignment.CENTER,
                                    true, BorderStyle.THIN, cBlueLight)
        val stValueZ    = makeStyle(cBluePale, fValue, HorizontalAlignment.LEFT,   VerticalAlignment.CENTER,
                                    true, BorderStyle.THIN, cBlueLight)
        val stValueC    = makeStyle(null,      fValue, HorizontalAlignment.CENTER, VerticalAlignment.CENTER,
                                    false, BorderStyle.THIN, cBlueLight)
        val stValueCZ   = makeStyle(cBluePale, fValue, HorizontalAlignment.CENTER, VerticalAlignment.CENTER,
                                    false, BorderStyle.THIN, cBlueLight)

        // Totales (naranja, texto blanco)
        val stTotal     = makeStyle(cOrange, fTotal, HorizontalAlignment.CENTER, VerticalAlignment.CENTER,
                                    false, BorderStyle.THIN, cBlueDark)

        // Pie de página
        val stFooter    = makeStyle(null, fFooter, HorizontalAlignment.CENTER)
        val stCargo     = makeStyle(null, fCargo,  HorizontalAlignment.CENTER)

        // Línea de firma
        val stSignLine  = (workbook.createCellStyle() as XSSFCellStyle).apply {
            borderTop = BorderStyle.MEDIUM
            setTopBorderColor(cBlueMid)
            setFont(makeFont("Calibri", 10, true, cBlueDark))
            alignment         = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
        }

        // ── Único drawing patriarch para todas las imágenes ────────────────────
        val drawing = sheet.createDrawingPatriarch()

        // ── Ancho de columnas ──────────────────────────────────────────────────
        sheet.setColumnWidth(0, 6000)
        sheet.setColumnWidth(1, 4200)
        sheet.setColumnWidth(2, 4200)
        sheet.setColumnWidth(3, 6000)
        sheet.setColumnWidth(4, 4200)
        sheet.setColumnWidth(5, 4200)

        // ── Contador de filas y helpers ────────────────────────────────────────
        var r = 0

        fun merge(r1: Int, r2: Int, c1: Int, c2: Int) =
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(r1, r2, c1, c2))

        fun Row.cell(col: Int, value: String,  st: XSSFCellStyle) =
            createCell(col).apply { setCellValue(value); cellStyle = st }

        fun Row.cell(col: Int, value: Double,  st: XSSFCellStyle) =
            createCell(col).apply { setCellValue(value); cellStyle = st }

        fun Row.fill(col: Int, st: XSSFCellStyle) =
            createCell(col).apply { cellStyle = st }

        fun sectionHeader(title: String) {
            sheet.createRow(r).also { row ->
                row.heightInPoints = 24f
                row.cell(0, title, stSection)
                merge(r, r, 0, 5)
            }
        }

        fun orangeStripe() {
            sheet.createRow(r).also { row ->
                row.heightInPoints = 3f
                for (c in 0..5) row.createCell(c).cellStyle = stOrangeBar
            }
        }

        val reportTitle = report.proyecto.ifBlank { report.title.ifBlank { "SIN NOMBRE" } }

        // ══════════════════════════════════════════════════════════════════════
        // FILA 0 — ENCABEZADO: Logo + Título
        // ══════════════════════════════════════════════════════════════════════
        sheet.createRow(r).also { row ->
            row.heightInPoints = 65f
            row.fill(0, stHeaderFill)
            row.fill(1, stHeaderFill)
            merge(r, r, 0, 1)
            row.cell(2, "REPORTE DIARIO DE OBRA\n$reportTitle", stHeader)
            merge(r, r, 2, 5)
        }

        // Logo CEI sobre las celdas 0-1
        try {
            val logoResId = context.resources.getIdentifier("logocei", "drawable", context.packageName)
            if (logoResId != 0) {
                val logoBmp = BitmapFactory.decodeResource(context.resources, logoResId)
                if (logoBmp != null) {
                    val logoStream = ByteArrayOutputStream()
                    logoBmp.compress(Bitmap.CompressFormat.PNG, 100, logoStream)
                    val pictureIdx = workbook.addPicture(logoStream.toByteArray(), Workbook.PICTURE_TYPE_PNG)
                    val anchor = workbook.creationHelper.createClientAnchor().apply {
                        setCol1(0); setRow1(r); setCol2(2); setRow2(r + 1)
                    }
                    drawing.createPicture(anchor, pictureIdx)
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        r++

        // FILA 1 — Barra de subtítulo con fecha de generación
        val nowShort = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        sheet.createRow(r).also { row ->
            row.heightInPoints = 18f
            row.fill(0, stHeaderFill); row.fill(1, stHeaderFill)
            row.fill(2, stHeaderFill); row.fill(3, stHeaderFill)
            merge(r, r, 0, 3)
            row.cell(4, "Generado: $nowShort", stSubBar)
            merge(r, r, 4, 5)
        }
        r++

        // Congelar las 2 primeras filas al hacer scroll
        sheet.createFreezePane(0, 2)

        // Raya naranja separadora
        orangeStripe(); r++

        // ══════════════════════════════════════════════════════════════════════
        // DATOS GENERALES DEL PROYECTO
        // ══════════════════════════════════════════════════════════════════════
        sectionHeader("DATOS GENERALES DEL PROYECTO"); r++

        val leftFields = listOf(
            "Proyecto:"    to reportTitle,
            "Fase:"        to report.fase,
            "Área:"        to report.area,
            "Sistema:"     to report.sistema,
            "Disciplina:"  to report.disciplina,
            "Responsable:" to report.technicianName
        )
        val rightFields = listOf(
            "Fecha:"          to report.date,
            "No. Contrato:"   to report.noContrato,
            "Ubicación GPS:"  to report.location,
            "Alcance:"        to report.descripcionAlcance
        )

        val maxMeta = maxOf(leftFields.size, rightFields.size)
        for (i in 0 until maxMeta) {
            sheet.createRow(r).also { row ->
                row.heightInPoints = 20f
                val vSt = if (i % 2 == 0) stValue else stValueZ

                if (i < leftFields.size) {
                    val (lbl, v) = leftFields[i]
                    row.cell(0, lbl, stLabel)
                    row.cell(1, v, vSt); merge(r, r, 1, 2)
                } else {
                    row.fill(0, stLabel); row.fill(1, vSt); merge(r, r, 1, 2)
                }

                if (i < rightFields.size) {
                    val (lbl, v) = rightFields[i]
                    row.cell(3, lbl, stLabel)
                    row.cell(4, v, vSt); merge(r, r, 4, 5)
                } else {
                    row.fill(3, stLabel); row.fill(4, vSt); merge(r, r, 4, 5)
                }
            }
            r++
        }
        r++

        // ══════════════════════════════════════════════════════════════════════
        // SEGURIDAD Y CONDICIONES CLIMÁTICAS
        // ══════════════════════════════════════════════════════════════════════
        sectionHeader("SEGURIDAD Y CONDICIONES CLIMÁTICAS"); r++

        val climaMap = mapOf(
            "soleado"  to "☀️ Soleado",        "parcial"  to "⛅ Parc. Nublado",
            "nublado"  to "☁️ Nublado",         "lluvioso" to "🌧️ Lluvioso",
            "tormenta" to "⛈️ Tormenta",        "neblina"  to "🌫️ Neblina",
            "ventoso"  to "💨 Ventoso",          "caluroso" to "🌡️ Caluroso",
            "frio"     to "❄️ Frío"
        )
        val climaTexto = report.clima.joinToString("   |   ") { climaMap[it] ?: it }.ifBlank { "No especificado" }

        sheet.createRow(r).also { row ->
            row.heightInPoints = 22f
            row.cell(0, "Condición Climática:", stLabel)
            row.cell(1, climaTexto, stValue); merge(r, r, 1, 5)
        }; r++

        // Sub-sección: Actividades de seguridad
        sheet.createRow(r).also { row ->
            row.heightInPoints = 20f
            row.cell(0, "Actividades de Seguridad:", stColHeader); merge(r, r, 0, 5)
        }; r++

        if (report.actividadesSeguridad.isNotEmpty()) {
            report.actividadesSeguridad.forEachIndexed { idx, act ->
                sheet.createRow(r).also { row ->
                    row.heightInPoints = 20f
                    val vSt = if (idx % 2 == 0) stValue else stValueZ
                    row.cell(0, "${idx + 1}.", stLabel)
                    row.cell(1, act, vSt); merge(r, r, 1, 5)
                }; r++
            }
        } else {
            sheet.createRow(r).also { row ->
                row.heightInPoints = 18f
                row.cell(0, "Sin actividades de seguridad registradas", stValue); merge(r, r, 0, 5)
            }; r++
        }
        r++

        // ══════════════════════════════════════════════════════════════════════
        // ACTIVIDADES REALIZADAS
        // ══════════════════════════════════════════════════════════════════════
        sectionHeader("ACTIVIDADES REALIZADAS"); r++

        if (report.actividadesRealizadas.isNotEmpty()) {
            report.actividadesRealizadas.forEachIndexed { idx, act ->
                sheet.createRow(r).also { row ->
                    row.heightInPoints = 22f
                    val vSt = if (idx % 2 == 0) stValue else stValueZ
                    row.cell(0, "${idx + 1}.", stLabel)
                    row.cell(1, act, vSt); merge(r, r, 1, 5)
                }; r++
            }
        } else {
            sheet.createRow(r).also { row ->
                row.heightInPoints = 18f
                row.cell(0, "Sin actividades registradas", stValue); merge(r, r, 0, 5)
            }; r++
        }
        r++

        // ══════════════════════════════════════════════════════════════════════
        // OBSERVACIONES Y NOTAS DE CAMPO
        // ══════════════════════════════════════════════════════════════════════
        if (report.observacionesList.isNotEmpty()) {
            sectionHeader("OBSERVACIONES Y NOTAS DE CAMPO"); r++
            report.observacionesList.forEachIndexed { idx, obs ->
                sheet.createRow(r).also { row ->
                    row.heightInPoints = 22f
                    val vSt = if (idx % 2 == 0) stValue else stValueZ
                    row.cell(0, "${idx + 1}.", stLabel)
                    row.cell(1, obs, vSt); merge(r, r, 1, 5)
                }; r++
            }
            r++
        }

        // ══════════════════════════════════════════════════════════════════════
        // FUERZA DE TRABAJO
        // ══════════════════════════════════════════════════════════════════════
        sectionHeader("FUERZA DE TRABAJO"); r++

        // Encabezado de columnas
        sheet.createRow(r).also { row ->
            row.heightInPoints = 22f
            row.cell(0, "Rol / Puesto",  stColHeader); merge(r, r, 0, 2)
            row.cell(3, "Cantidad",      stColHeader); merge(r, r, 3, 4)
            row.cell(5, "Horas Trab.",   stColHeader)
        }; r++

        val rolesNombres = listOf(
            "Sup. Seguridad", "Residente de Obra", "Op. de Planta",
            "Topógrafo", "Cadenero", "Oficiales",
            "Ayudantes", "Banderero", "Sup. de Obra", "Sup. Calidad"
        )

        var totalCantWf = 0; var totalHrsWf = 0.0
        rolesNombres.forEachIndexed { idx, rol ->
            val cant = report.fuerzaTrabajoCantidades.getOrElse(idx) { "" }.toIntOrNull() ?: 0
            val hor  = report.fuerzaTrabajoHoras.getOrElse(idx)       { "" }.toDoubleOrNull() ?: 0.0
            totalCantWf += cant; totalHrsWf += hor

            val vSt  = if (idx % 2 == 0) stValue  else stValueZ
            val vStC = if (idx % 2 == 0) stValueC else stValueCZ
            sheet.createRow(r).also { row ->
                row.heightInPoints = 20f
                row.cell(0, rol,             stLabel);  merge(r, r, 0, 2)
                row.cell(3, cant.toDouble(), vStC);     merge(r, r, 3, 4)
                row.cell(5, hor,             vStC)
            }; r++
        }

        // Fila TOTAL naranja
        sheet.createRow(r).also { row ->
            row.heightInPoints = 24f
            row.cell(0, "TOTAL FUERZA DE TRABAJO",        stTotal); merge(r, r, 0, 2)
            row.cell(3, totalCantWf.toDouble(),            stTotal); merge(r, r, 3, 4)
            row.cell(5, "${"%.1f".format(totalHrsWf)} hrs", stTotal)
        }; r += 2

        // ══════════════════════════════════════════════════════════════════════
        // MAQUINARIA UTILIZADA
        // ══════════════════════════════════════════════════════════════════════
        sectionHeader("MAQUINARIA UTILIZADA"); r++

        sheet.createRow(r).also { row ->
            row.heightInPoints = 22f
            row.cell(0, "Maquinaria / Equipo", stColHeader); merge(r, r, 0, 2)
            row.cell(3, "Cantidad",            stColHeader); merge(r, r, 3, 4)
            row.cell(5, "Horas Máq.",          stColHeader)
        }; r++

        val equiposNombres = listOf(
            "Bailarina", "Hormigonera", "Minicar", "Vehículos",
            "Generador", "Rotomartillo", "Compresor"
        )

        var totalCantMac = 0; var totalHrsMac = 0.0
        equiposNombres.forEachIndexed { idx, equipo ->
            val cant = report.maquinariaCantidades.getOrElse(idx) { "" }.toIntOrNull() ?: 0
            val hor  = report.maquinariaHoras.getOrElse(idx)      { "" }.toDoubleOrNull() ?: 0.0
            totalCantMac += cant; totalHrsMac += hor

            val vSt  = if (idx % 2 == 0) stValue  else stValueZ
            val vStC = if (idx % 2 == 0) stValueC else stValueCZ
            sheet.createRow(r).also { row ->
                row.heightInPoints = 20f
                row.cell(0, equipo,          stLabel);  merge(r, r, 0, 2)
                row.cell(3, cant.toDouble(), vStC);     merge(r, r, 3, 4)
                row.cell(5, hor,             vStC)
            }; r++
        }

        sheet.createRow(r).also { row ->
            row.heightInPoints = 24f
            row.cell(0, "TOTAL MAQUINARIA",                stTotal); merge(r, r, 0, 2)
            row.cell(3, totalCantMac.toDouble(),            stTotal); merge(r, r, 3, 4)
            row.cell(5, "${"%.1f".format(totalHrsMac)} hrs", stTotal)
        }; r += 2

        // ══════════════════════════════════════════════════════════════════════
        // ACTIVIDADES PLANEADAS — SIGUIENTE DÍA
        // ══════════════════════════════════════════════════════════════════════
        sectionHeader("ACTIVIDADES PLANEADAS — SIGUIENTE DÍA"); r++

        if (report.actividadesPlaneadas.isNotEmpty()) {
            report.actividadesPlaneadas.forEachIndexed { idx, act ->
                sheet.createRow(r).also { row ->
                    row.heightInPoints = 22f
                    val vSt = if (idx % 2 == 0) stValue else stValueZ
                    row.cell(0, "${idx + 1}.", stLabel)
                    row.cell(1, act, vSt); merge(r, r, 1, 5)
                }; r++
            }
        } else {
            sheet.createRow(r).also { row ->
                row.heightInPoints = 18f
                row.cell(0, "Sin actividades planeadas registradas", stValue); merge(r, r, 0, 5)
            }; r++
        }
        r++

        // ══════════════════════════════════════════════════════════════════════
        // EVIDENCIAS FOTOGRÁFICAS
        // ══════════════════════════════════════════════════════════════════════
        if (report.photos.isNotEmpty()) {
            sectionHeader("EVIDENCIAS FOTOGRÁFICAS"); r++
            r++ // espacio

            var photoCol = 0
            report.photos.forEachIndexed { index, photoPath ->
                val file = File(photoPath)
                val caption = report.photoCaptions.getOrElse(index) { "" }

                if (file.exists()) {
                    try {
                        val bytes = compressImage(file, 500, 375)
                        if (bytes != null) {
                            val pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG)
                            val anchor = workbook.creationHelper.createClientAnchor().apply {
                                setCol1(photoCol); setRow1(r)
                                setCol2(photoCol + 3); setRow2(r + 9)
                            }
                            drawing.createPicture(anchor, pictureIdx)
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }

                if (caption.isNotBlank()) {
                    val capRow = sheet.getRow(r + 9) ?: sheet.createRow(r + 9)
                    capRow.heightInPoints = 18f
                    capRow.createCell(photoCol).apply {
                        setCellValue("📷 $caption"); cellStyle = stValue
                    }
                    sheet.addMergedRegion(
                        org.apache.poi.ss.util.CellRangeAddress(r + 9, r + 9, photoCol, photoCol + 2)
                    )
                }

                photoCol += 3
                if (photoCol >= 6) { photoCol = 0; r += 11 }
            }
            if (photoCol != 0) r += 11
            r++
        }

        // ══════════════════════════════════════════════════════════════════════
        // CROQUIS DESCRIPTIVO
        // ══════════════════════════════════════════════════════════════════════
        if (!report.croquisPath.isNullOrEmpty()) {
            val croquisFile = File(report.croquisPath)
            if (croquisFile.exists()) {
                sectionHeader("CROQUIS DESCRIPTIVO"); r++
                r++
                try {
                    val bytes = compressImage(croquisFile, 700, 500)
                    if (bytes != null) {
                        val pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG)
                        val anchor = workbook.creationHelper.createClientAnchor().apply {
                            setCol1(0); setRow1(r); setCol2(6); setRow2(r + 15)
                        }
                        drawing.createPicture(anchor, pictureIdx)
                        r += 16
                    }
                } catch (e: Exception) { e.printStackTrace() }
                r++
            }
        }

        // ══════════════════════════════════════════════════════════════════════
        // AVANCE POR ÁREA / DISCIPLINA
        // ══════════════════════════════════════════════════════════════════════
        if (report.areasAvance.isNotEmpty() && report.areasAvance.any { it.isNotBlank() }) {
            sectionHeader("AVANCE POR ÁREA / DISCIPLINA"); r++

            sheet.createRow(r).also { row ->
                row.heightInPoints = 22f
                row.cell(0, "Área / Disciplina", stColHeader); merge(r, r, 0, 4)
                row.cell(5, "% Avance",          stColHeader)
            }; r++

            report.areasAvance.forEachIndexed { idx, area ->
                if (area.isNotBlank()) {
                    val pct  = report.avancePorcentajes.getOrElse(idx) { "0" }
                    val vSt  = if (idx % 2 == 0) stValue  else stValueZ
                    val vStC = if (idx % 2 == 0) stValueC else stValueCZ
                    sheet.createRow(r).also { row ->
                        row.heightInPoints = 20f
                        row.cell(0, area,    vSt);  merge(r, r, 0, 4)
                        row.cell(5, "$pct%", vStC)
                    }; r++
                }
            }
            r++
        }

        // ══════════════════════════════════════════════════════════════════════
        // FIRMAS DE CONFORMIDAD
        // ══════════════════════════════════════════════════════════════════════
        val hasSuperSig    = !report.supervisorSignaturePath.isNullOrEmpty() &&
                              File(report.supervisorSignaturePath!!).exists()
        val hasContractSig = !report.signaturePath.isNullOrEmpty() &&
                              File(report.signaturePath!!).exists()

        if (hasSuperSig || hasContractSig) {
            sectionHeader("FIRMAS DE CONFORMIDAD"); r++
            r++ // espacio antes de las firmas

            val sigStartRow = r

            if (hasSuperSig) {
                try {
                    val bytes = compressImage(File(report.supervisorSignaturePath!!), 280, 130)
                    if (bytes != null) {
                        val pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG)
                        val anchor = workbook.creationHelper.createClientAnchor().apply {
                            setCol1(0); setRow1(sigStartRow); setCol2(3); setRow2(sigStartRow + 5)
                        }
                        drawing.createPicture(anchor, pictureIdx)
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }

            if (hasContractSig) {
                try {
                    val bytes = compressImage(File(report.signaturePath!!), 280, 130)
                    if (bytes != null) {
                        val pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG)
                        val anchor = workbook.creationHelper.createClientAnchor().apply {
                            setCol1(3); setRow1(sigStartRow); setCol2(6); setRow2(sigStartRow + 5)
                        }
                        drawing.createPicture(anchor, pictureIdx)
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }

            r += 5

            // Línea de firma + nombre
            sheet.createRow(r).also { row ->
                row.heightInPoints = 22f
                val supName = report.supervisor.ifBlank { "Supervisor" }
                val conName = report.technicianName.ifBlank { "Responsable de Contratista" }
                row.createCell(0).apply { setCellValue(supName); cellStyle = stSignLine }
                merge(r, r, 0, 2)
                row.createCell(3).apply { setCellValue(conName); cellStyle = stSignLine }
                merge(r, r, 3, 5)
            }; r++

            // Cargo
            sheet.createRow(r).also { row ->
                row.heightInPoints = 16f
                row.cell(0, "Supervisor de Proyecto",    stCargo); merge(r, r, 0, 2)
                row.cell(3, "Responsable de Contratista", stCargo); merge(r, r, 3, 5)
            }; r += 2
        }

        // ══════════════════════════════════════════════════════════════════════
        // PIE DE PÁGINA
        // ══════════════════════════════════════════════════════════════════════
        orangeStripe(); r++

        val nowFull = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        sheet.createRow(r).also { row ->
            row.heightInPoints = 18f
            row.cell(0, "Generado por CEIReport   •   $nowFull   •   Documento Confidencial", stFooter)
            merge(r, r, 0, 5)
        }

        // ── Guardar archivo ────────────────────────────────────────────────────
        val outputDir = File(context.getExternalFilesDir(null), "Reports")
        if (!outputDir.exists()) outputDir.mkdirs()
        val outFile = File(outputDir, "Reporte_${report.id}_${System.currentTimeMillis()}.xlsx")
        FileOutputStream(outFile).use { workbook.write(it) }
        workbook.close()
        return outFile
    }

    // ── Compresión de imágenes ─────────────────────────────────────────────────
    private fun compressImage(file: File, width: Int, height: Int): ByteArray? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, opts)
        var sample = 1
        if (opts.outHeight > height || opts.outWidth > width) {
            val hh = opts.outHeight / 2; val hw = opts.outWidth / 2
            while ((hh / sample) >= height && (hw / sample) >= width) sample *= 2
        }
        opts.inJustDecodeBounds = false; opts.inSampleSize = sample
        val bmp = BitmapFactory.decodeFile(file.absolutePath, opts) ?: return null
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        bmp.recycle()
        return stream.toByteArray()
    }
}
