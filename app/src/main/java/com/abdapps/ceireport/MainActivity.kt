package com.abdapps.ceireport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.abdapps.ceireport.data.local.ReportDatabase
import com.abdapps.ceireport.data.repository.ReportRepository
import com.abdapps.ceireport.ui.screens.ActivitiesObservationsScreen
import com.abdapps.ceireport.ui.screens.FinalizeScreen
import com.abdapps.ceireport.ui.screens.GeneralDataScreen
import com.abdapps.ceireport.ui.screens.MachineryScreen
import com.abdapps.ceireport.ui.screens.PlannedActivitiesScreen
import com.abdapps.ceireport.ui.screens.ReportFormScreen
import com.abdapps.ceireport.ui.screens.ReportListScreen
import com.abdapps.ceireport.ui.screens.SafetyWeatherScreen
import com.abdapps.ceireport.ui.screens.SplashScreen
import com.abdapps.ceireport.ui.screens.WorkforceScreen
import com.abdapps.ceireport.ui.theme.CEIReportTheme
import com.abdapps.ceireport.ui.viewmodel.ReportViewModel
import com.abdapps.ceireport.ui.viewmodel.ReportViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val database by lazy { ReportDatabase.getDatabase(this) }
    private val repository by lazy { ReportRepository(database.reportDao()) }
    private val viewModel: ReportViewModel by viewModels {
        ReportViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CEIReportTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("splash") }

                    when (currentScreen) {
                        // ── Splash ────────────────────────────────────────────
                        "splash" -> {
                            SplashScreen(
                                onFinished = { currentScreen = "list" }
                            )
                        }
                        // ── Pantalla: Lista de Reportes ───────────────────────
                        "list" -> {
                            ReportListScreen(
                                viewModel = viewModel,
                                onNavigateToForm = { currentScreen = "formFlow" }
                            )
                        }
                        // ── Flujo de Formulario con Deslizamiento (Pager) ─────
                        "formFlow" -> {
                            FormPagerFlow(
                                viewModel = viewModel,
                                onExitFlow = { currentScreen = "list" }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormPagerFlow(
    viewModel: ReportViewModel,
    onExitFlow: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 8 })
    val coroutineScope = rememberCoroutineScope()

    fun scrollToPage(targetPage: Int) {
        if (targetPage in 0..6) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(targetPage)
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = true // Permite deslizar libremente entre pantallas
    ) { page ->
        when (page) {
            // ── Paso 1: Datos Generales ───────────────────────────────
            0 -> {
                GeneralDataScreen(
                    viewModel = viewModel,
                    onNavigateBack = { onExitFlow() },
                    onNavigateNext = { scrollToPage(1) }
                )
            }
            // ── Paso 2: Seguridad y Clima ─────────────────────────────
            1 -> {
                SafetyWeatherScreen(
                    viewModel = viewModel,
                    onNavigateBack = { onExitFlow() },
                    onNavigateNext = { scrollToPage(2) }
                )
            }
            // ── Paso 3: Actividades Realizadas y Observaciones ────────
            2 -> {
                ActivitiesObservationsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { onExitFlow() },
                    onNavigateNext = { scrollToPage(3) }
                )
            }
            // ── Paso 4: Fuerza de Trabajo ─────────────────────────────
            3 -> {
                WorkforceScreen(
                    viewModel = viewModel,
                    onNavigateBack = { onExitFlow() },
                    onNavigateNext = { scrollToPage(4) }
                )
            }
            // ── Paso 5: Maquinaria Utilizada ──────────────────────────
            4 -> {
                MachineryScreen(
                    viewModel = viewModel,
                    onNavigateBack = { onExitFlow() },
                    onNavigateNext = { scrollToPage(5) }
                )
            }
            // ── Paso 6: Actividades Planeadas para el Siguiente Día ───
            5 -> {
                PlannedActivitiesScreen(
                    viewModel = viewModel,
                    onNavigateBack = { onExitFlow() },
                    onNavigateNext = { scrollToPage(6) }
                )
            }
            // ── Paso 7: Evidencias Fotográficas, Croquis y Firma ─────
            6 -> {
                ReportFormScreen(
                    viewModel = viewModel,
                    onNavigateBack = { onExitFlow() }
                )
            }
            // ── Paso 8: Avance, Responsables y Finalización ───────────
            7 -> {
                FinalizeScreen(
                    viewModel = viewModel,
                    onNavigateBack = { onExitFlow() }
                )
            }
        }
    }
}