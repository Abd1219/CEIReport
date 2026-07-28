package com.abdapps.ceireport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.abdapps.ceireport.data.local.ReportDatabase
import com.abdapps.ceireport.data.repository.ReportRepository
import com.abdapps.ceireport.ui.screens.ActivitiesObservationsScreen
import com.abdapps.ceireport.ui.screens.GeneralDataScreen
import com.abdapps.ceireport.ui.screens.ReportFormScreen
import com.abdapps.ceireport.ui.screens.ReportListScreen
import com.abdapps.ceireport.ui.screens.SafetyWeatherScreen
import com.abdapps.ceireport.ui.screens.SplashScreen
import com.abdapps.ceireport.ui.screens.WorkforceScreen
import com.abdapps.ceireport.ui.theme.CEIReportTheme
import com.abdapps.ceireport.ui.viewmodel.ReportViewModel
import com.abdapps.ceireport.ui.viewmodel.ReportViewModelFactory

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
                                onNavigateToForm = { currentScreen = "generalData" }
                            )
                        }
                        // ── Pantalla 1: Datos Generales ───────────────────────
                        "generalData" -> {
                            GeneralDataScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "list" },
                                onNavigateNext = { currentScreen = "safety" }
                            )
                        }
                        // ── Pantalla 2: Seguridad y Clima ─────────────────
                        "safety" -> {
                            SafetyWeatherScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "generalData" },
                                onNavigateNext = { currentScreen = "activities" }
                            )
                        }
                        // ── Pantalla 3: Actividades Realizadas y Observaciones ──
                        "activities" -> {
                            ActivitiesObservationsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "safety" },
                                onNavigateNext = { currentScreen = "workforce" }
                            )
                        }
                        // ── Pantalla 4: Fuerza de Trabajo ────────────────────────
                        "workforce" -> {
                            WorkforceScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "activities" },
                                onNavigateNext = { currentScreen = "form" }
                            )
                        }
                        // ── Pantalla 5: Evidencias Fotográficas y Firma ──────
                        "form" -> {
                            ReportFormScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "workforce" }
                            )
                        }
                    }
                }
            }
        }
    }
}