package com.abdapps.ceireport.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    onSignatureSaved: (Bitmap) -> Unit,
    onDismiss: () -> Unit
) {
    var points = remember { mutableStateListOf<Offset?>() }
    var canvasWidth by remember { mutableStateOf(1f) }
    var canvasHeight by remember { mutableStateOf(1f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Dibuje su Firma",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .background(androidx.compose.ui.graphics.Color.White)
                .pointerInteropFilter { event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            points.add(Offset(event.x, event.y))
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            points.add(Offset(event.x, event.y))
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            points.add(null) // separator
                            true
                        }
                        else -> false
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                canvasWidth = size.width
                canvasHeight = size.height
                for (i in 0 until points.size - 1) {
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    if (p1 != null && p2 != null) {
                        drawLine(
                            color = androidx.compose.ui.graphics.Color.Black,
                            start = p1,
                            end = p2,
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
            Button(
                onClick = { points.clear() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Limpiar")
            }
            Button(
                onClick = {
                    if (points.isNotEmpty()) {
                        // Create bitmap of signature matching the exact drawn width & height
                        val w = if (canvasWidth > 0) canvasWidth.toInt() else 400
                        val h = if (canvasHeight > 0) canvasHeight.toInt() else 200
                        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)
                        
                        val paint = Paint().apply {
                            color = Color.BLACK
                            strokeWidth = 6f
                            strokeCap = Paint.Cap.ROUND
                            strokeJoin = Paint.Join.ROUND
                            style = Paint.Style.STROKE
                            isAntiAlias = true
                        }
                        
                        val path = android.graphics.Path()
                        var first = true
                        for (point in points) {
                            if (point == null) {
                                first = true
                            } else {
                                val x = point.x.coerceIn(0f, w.toFloat())
                                val y = point.y.coerceIn(0f, h.toFloat())
                                if (first) {
                                    path.moveTo(x, y)
                                    first = false
                                } else {
                                    path.lineTo(x, y)
                                }
                            }
                        }
                        canvas.drawPath(path, paint)
                        onSignatureSaved(bitmap)
                    }
                }
            ) {
                Text("Guardar Firma")
            }
        }
    }
}
