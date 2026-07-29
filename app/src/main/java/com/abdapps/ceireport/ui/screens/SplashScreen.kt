package com.abdapps.ceireport.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abdapps.ceireport.R
import kotlinx.coroutines.delay

// ── Colores del splash ────────────────────────────────────────────────────────
private val SplashBgTop    = Color(0xFF234499)   // HeaderBlueDark
private val SplashBgBottom = Color(0xFF193275)   // Azul marino profundo
private val SplashGlow     = Color(0xFF3461C7)   // HeaderBlue
private val OrangeBadge    = Color(0xFFEA5B29)   // AccentOrange

@Composable
fun SplashScreen(onFinished: () -> Unit) {

    // ── Animación de progreso ─────────────────────────────────────────────────
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
        label = "progressAnim"
    )

    // ── Animación de entrada (fade + slide) ───────────────────────────────────
    val logoAlpha by animateFloatAsState(
        targetValue = if (progress > 0f) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "logoAlpha"
    )
    val logoOffset by animateFloatAsState(
        targetValue = if (progress > 0f) 0f else 40f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "logoOffset"
    )

    LaunchedEffect(Unit) {
        delay(100)
        progress = 1f          // dispara la animación al 100 %
        delay(2600)            // espera a que llegue al 100 % + pequeña pausa
        onFinished()
    }

    // ── Layout principal ──────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SplashBgTop, SplashBgBottom)
                )
            )
    ) {
        // Círculo decorativo superior izquierdo
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-60).dp, y = (-60).dp)
                .clip(CircleShape)
                .background(SplashGlow.copy(alpha = 0.18f))
        )
        // Círculo decorativo superior derecho
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = 30.dp)
                .clip(CircleShape)
                .background(SplashGlow.copy(alpha = 0.13f))
        )

        // ── Contenido central ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Icono principal
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .graphicsLayerCompat(alpha = logoAlpha, translationY = logoOffset),
                contentAlignment = Alignment.BottomEnd
            ) {
                // Fondo contenedor con contraste para destacar el logo PNG
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF132B66))
                        .border(1.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logocei),
                        contentDescription = "Logo CEI",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(72.dp)
                            .padding(4.dp)
                    )
                }
                // Badge naranja con ícono de clipboard
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(OrangeBadge),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Article,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // CEI
            Text(
                text = "CEI",
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 4.sp,
                modifier = Modifier.graphicsLayerCompat(alpha = logoAlpha, translationY = logoOffset)
            )

            // REPORTE DIARIO
            Text(
                text = "REPORTE DIARIO",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f),
                letterSpacing = 6.sp,
                modifier = Modifier.graphicsLayerCompat(alpha = logoAlpha, translationY = logoOffset)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtítulo
            Text(
                text = "Sistema de Reportes de Campo",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayerCompat(alpha = logoAlpha, translationY = logoOffset)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Barra de progreso
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayerCompat(alpha = logoAlpha),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Iniciando sistema...",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Pie de página ──────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Constructora CEI S.A. de C.V.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "v1.0  ·  © 2025",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.40f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Desarrollado por AbdApps",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = OrangeBadge.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// Extensión helper para aplicar alpha + translationY
private fun Modifier.graphicsLayerCompat(
    alpha: Float = 1f,
    translationY: Float = 0f
): Modifier = this.graphicsLayer(
    alpha = alpha,
    translationY = translationY
)
