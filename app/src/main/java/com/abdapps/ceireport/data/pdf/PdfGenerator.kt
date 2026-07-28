package com.abdapps.ceireport.data.pdf

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.abdapps.ceireport.data.model.Report
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generate(context: Context, report: Report): File {
        val pdfDocument = PdfDocument()

        // Page size: Letter (612 x 792 points)
        val pageWidth = 612
        val pageHeight = 792

        // Paint configurations
        val titlePaint = Paint().apply {
            color = Color.parseColor("#002060") // Dark Blue
            style = Paint.Style.FILL
        }

        val textPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        val boldTextPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
            isFakeBoldText = true
        }

        val labelBgPaint = Paint().apply {
            color = Color.parseColor("#D9E1F2") // Light blue background
            style = Paint.Style.FILL
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        // --- PAGE 1: METADATA & CONTENT ---
        val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        val canvas1 = page1.canvas

        var currentY = 40f
        val margin = 40f
        val contentWidth = pageWidth - (margin * 2)

        // Title
        canvas1.drawRect(margin, currentY, margin + contentWidth, currentY + 45f, titlePaint)
        val titleTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas1.drawText("REPORTE DIARIO CEI", pageWidth / 2f, currentY + 28f, titleTextPaint)
        currentY += 65f

        // Info Fields
        val fields = listOf(
            "Título del Reporte" to report.title,
            "Fecha" to report.date,
            "Técnico Responsable" to report.technicianName,
            "Ubicación" to report.location
        )

        for ((label, value) in fields) {
            // Background for label
            canvas1.drawRect(margin, currentY, margin + 150f, currentY + 24f, labelBgPaint)
            canvas1.drawRect(margin, currentY, margin + contentWidth, currentY + 24f, linePaint)
            canvas1.drawRect(margin, currentY, margin + 150f, currentY + 24f, linePaint)

            canvas1.drawText(label, margin + 8f, currentY + 16f, boldTextPaint)
            canvas1.drawText(value, margin + 158f, currentY + 16f, textPaint)

            currentY += 24f
        }
        currentY += 20f

        // Description Section
        canvas1.drawText("Actividades Realizadas / Descripción:", margin, currentY, boldTextPaint)
        currentY += 10f
        val descLayout = StaticLayout.Builder.obtain(
            report.description, 0, report.description.length, textPaint, contentWidth.toInt()
        ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

        canvas1.save()
        canvas1.translate(margin, currentY)
        descLayout.draw(canvas1)
        canvas1.restore()
        currentY += descLayout.height + 25f

        // Observations Section
        if (report.observations.isNotEmpty()) {
            canvas1.drawText("Observaciones:", margin, currentY, boldTextPaint)
            currentY += 10f
            val obsLayout = StaticLayout.Builder.obtain(
                report.observations, 0, report.observations.length, textPaint, contentWidth.toInt()
            ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

            canvas1.save()
            canvas1.translate(margin, currentY)
            obsLayout.draw(canvas1)
            canvas1.restore()
            currentY += obsLayout.height + 25f
        }

        // Signature Section
        if (!report.signaturePath.isNullOrEmpty()) {
            val sigFile = File(report.signaturePath)
            if (sigFile.exists()) {
                val sigY = pageHeight - 150f
                canvas1.drawLine(margin + 120f, sigY, margin + 360f, sigY, linePaint)
                canvas1.drawText("Firma del Técnico", margin + 195f, sigY + 18f, boldTextPaint)

                try {
                    val sigBitmap = BitmapFactory.decodeFile(sigFile.absolutePath)
                    if (sigBitmap != null) {
                        val destRect = Rect(
                            (margin + 160f).toInt(),
                            (sigY - 70f).toInt(),
                            (margin + 320f).toInt(),
                            sigY.toInt()
                        )
                        canvas1.drawBitmap(sigBitmap, null, destRect, null)
                        sigBitmap.recycle()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        pdfDocument.finishPage(page1)

        // --- PAGE 2: EVIDENCE PHOTOS (If photos exist) ---
        if (report.photos.isNotEmpty()) {
            val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
            val page2 = pdfDocument.startPage(pageInfo2)
            val canvas2 = page2.canvas

            var photoY = 40f
            canvas2.drawText("Evidencias Fotográficas:", margin, photoY, boldTextPaint)
            photoY += 25f

            // Lay photos in a grid (2 columns)
            val photoWidth = (contentWidth - 20) / 2
            val photoHeight = photoWidth * 0.75f // 4:3 Aspect ratio

            var col = 0
            for (photoPath in report.photos) {
                val file = File(photoPath)
                if (file.exists()) {
                    try {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        if (bitmap != null) {
                            val left = margin + col * (photoWidth + 20)
                            val top = photoY
                            val right = left + photoWidth
                            val bottom = top + photoHeight

                            val destRect = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
                            canvas2.drawBitmap(bitmap, null, destRect, null)
                            canvas2.drawRect(left, top, right, bottom, linePaint)

                            bitmap.recycle()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                col++
                if (col >= 2) {
                    col = 0
                    photoY += photoHeight + 20f
                }

                // If we run out of page space, we could expand, but let's assume up to 4 photos fit easily
                if (photoY + photoHeight > pageHeight - 40f) {
                    break
                }
            }

            pdfDocument.finishPage(page2)
        }

        // Save PDF to file
        val outputDir = File(context.getExternalFilesDir(null), "Reports")
        if (!outputDir.exists()) outputDir.mkdirs()
        val file = File(outputDir, "Reporte_${report.id}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }
}
