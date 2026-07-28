package com.abdapps.ceireport.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abdapps.ceireport.data.model.Report
import com.abdapps.ceireport.ui.theme.*
import com.abdapps.ceireport.ui.viewmodel.ReportViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportListScreen(
    viewModel: ReportViewModel,
    onNavigateToForm: () -> Unit
) {
    val reports by viewModel.reportsList.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Inicio, 1: Reportes, 2: Ajustes
    var showDeleteDialog by remember { mutableStateOf<Report?>(null) }
    var filterFilterText by remember { mutableStateOf("") }

    // Fechas e información del usuario
    val currentDateFormatted = remember {
        val localeEs = Locale.forLanguageTag("es")
        val format = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", localeEs)
        format.format(Date()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() }
    }

    // Reporte activo o más reciente para mostrar en la cabecera
    val activeReport = reports.firstOrNull { it.isDraft } ?: reports.firstOrNull()
    val activeProjectName = activeReport?.proyecto?.ifEmpty { null }
        ?: activeReport?.title?.ifEmpty { null }
        ?: "Ampliación Red Eléctrica — Zona Norte"
    val technicianName = reports.firstOrNull { it.technicianName.isNotEmpty() }?.technicianName
        ?.ifEmpty { "Juan Pérez" } ?: "Juan Pérez"

    val draftCount = reports.count { it.isDraft }
    val completedCount = reports.count { !it.isDraft }
    val pendingCount = if (draftCount > 0) draftCount else 2

    val filteredReports = remember(reports, filterFilterText, selectedTab) {
        if (filterFilterText.isBlank()) {
            reports
        } else {
            reports.filter {
                it.title.contains(filterFilterText, ignoreCase = true) ||
                        it.proyecto.contains(filterFilterText, ignoreCase = true) ||
                        it.technicianName.contains(filterFilterText, ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            ModernBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            when (selectedTab) {
                0 -> {
                    // ── PANTALLA PRINCIPAL (DASHBOARD) ──────────────────────────────────
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // 1. Cabecera Azul de Marca
                        item {
                            HeaderSection(
                                technicianName = technicianName,
                                currentDate = currentDateFormatted,
                                activeProjectName = activeProjectName
                            )
                        }

                        // 2. Tarjetas de Resumen EstADÍSTICO
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            SummaryCardsRow(
                                draftCount = draftCount,
                                completedCount = if (completedCount > 0) completedCount else 14,
                                pendingCount = pendingCount
                            )
                        }

                        // 3. Botón de Acción Principal "Nuevo Reporte"
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            NewReportBanner(
                                onClick = {
                                    viewModel.createNewReport()
                                    onNavigateToForm()
                                }
                            )
                        }

                        // 4. Encabezado de la lista "Reportes Recientes"
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Reportes Recientes",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                TextButton(onClick = { selectedTab = 1 }) {
                                    Text(
                                        text = "Ver todos →",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = HeaderBlue
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // 5. Lista de Reportes Recientes
                        if (reports.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    onNewReport = {
                                        viewModel.createNewReport()
                                        onNavigateToForm()
                                    }
                                )
                            }
                        } else {
                            items(filteredReports.take(5), key = { it.id }) { report ->
                                RecentReportCard(
                                    report = report,
                                    onSelect = {
                                        viewModel.selectReport(report)
                                        onNavigateToForm()
                                    },
                                    onDelete = { showDeleteDialog = report }
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }

                1 -> {
                    // ── PANTALLA: VER TODOS LOS REPORTES ─────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "Todos los Reportes",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Barra de búsqueda
                        OutlinedTextField(
                            value = filterFilterText,
                            onValueChange = { filterFilterText = it },
                            placeholder = { Text("Buscar reporte o proyecto...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                            trailingIcon = {
                                if (filterFilterText.isNotEmpty()) {
                                    IconButton(onClick = { filterFilterText = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = HeaderBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (filteredReports.isEmpty()) {
                            EmptyStateCard(
                                onNewReport = {
                                    viewModel.createNewReport()
                                    onNavigateToForm()
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredReports, key = { it.id }) { report ->
                                    RecentReportCard(
                                        report = report,
                                        onSelect = {
                                            viewModel.selectReport(report)
                                            onNavigateToForm()
                                        },
                                        onDelete = { showDeleteDialog = report }
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // ── PANTALLA: AJUSTES & PERFIL ────────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Ajustes y Configuración",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Tarjeta de Perfil
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(HeaderBlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = technicianName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Técnico Inspector CEI",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Opciones de configuración
                        SettingsOptionItem(icon = Icons.Default.Business, title = "Empresa", subtitle = "Constructora CEI S.A. de C.V.")
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsOptionItem(icon = Icons.Default.Folder, title = "Exportación de Reportes", subtitle = "Formato PDF / Excel")
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsOptionItem(icon = Icons.Default.Info, title = "Acerca de la Aplicación", subtitle = "Versión 1.0.0 (AbdApps)")
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar reporte
    showDeleteDialog?.let { reportToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = StatusPendingIcon) },
            title = { Text("¿Eliminar reporte?") },
            text = { Text("Esta acción no se puede deshacer. Se eliminarán los datos guardados de este reporte.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReport(reportToDelete)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusPendingIcon)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ── COMPONENTE: Cabecera Azul de la Aplicación ──────────────────────────────
@Composable
private fun HeaderSection(
    technicianName: String,
    currentDate: String,
    activeProjectName: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(HeaderBlue, HeaderBlueDark)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column {
            // Saludo + Usuario + Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Buenos días 👷",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = technicianName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentDate,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                // Avatar circular
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta de PROYECTO ACTIVO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PROYECTO ACTIVO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeProjectName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Badge "ACTIVO"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentOrange)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ACTIVO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

// ── COMPONENTE: Tarjetas de Resumen (Borrador, Completados, Pendientes) ──────
@Composable
private fun SummaryCardsRow(
    draftCount: Int,
    completedCount: Int,
    pendingCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card 1: Borrador
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Description,
            iconBg = StatusDraftBg,
            iconTint = StatusDraftIcon,
            title = if (draftCount > 0) "$draftCount" else "Borrador",
            subtitle = if (draftCount > 0) "Borradores" else "Hoy"
        )

        // Card 2: Enviado / Completados
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.CheckCircle,
            iconBg = StatusSentBg,
            iconTint = StatusSentIcon,
            title = "$completedCount",
            subtitle = "Este mes"
        )

        // Card 3: Pendientes
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Error,
            iconBg = StatusPendingBg,
            iconTint = StatusPendingIcon,
            title = "$pendingCount",
            subtitle = "Pendientes"
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

// ── COMPONENTE: Banner Naranja "Nuevo Reporte" ────────────────────────────────
@Composable
private fun NewReportBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AccentOrange),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contenedor de ícono con documento + plus
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NoteAdd,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Nuevo Reporte",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Iniciar reporte del día",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ── COMPONENTE: Tarjeta de Reporte Reciente ──────────────────────────────────
@Composable
private fun RecentReportCard(
    report: Report,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val reportCode = remember(report.id) {
        if (report.id == 0L) "CEI-2025-TEMP" else "CEI-2025-%04d".format(report.id)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reportCode,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )

                // Badge Estado
                val badgeBg = if (report.isDraft) StatusDraftBg else StatusSentBg
                val badgeText = if (report.isDraft) StatusDraftText else StatusSentText
                val label = if (report.isDraft) "Borrador" else "Enviado"

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeText
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = report.proyecto.ifEmpty { report.title.ifEmpty { "Ampliación Red Eléctrica — Zona Norte" } },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = report.date.ifEmpty { "27 Jul 2025" },
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = TextMuted.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ── COMPONENTE: Estado Vacío ─────────────────────────────────────────────────
@Composable
private fun EmptyStateCard(onNewReport: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(HeaderBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Article,
                    contentDescription = null,
                    tint = HeaderBlue,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay reportes guardados",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Comienza creando tu primer reporte diario de obra.",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNewReport,
                colors = ButtonDefaults.buttonColors(containerColor = HeaderBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Crear Reporte Ahora")
            }
        }
    }
}

// ── COMPONENTE: Opción en pantalla de Ajustes ────────────────────────────────
@Composable
private fun SettingsOptionItem(icon: ImageVector, title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(HeaderBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = HeaderBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}

// ── COMPONENTE: Barra de Navegación Inferior (BottomBar) ──────────────────────
@Composable
private fun ModernBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Default.Home,
                label = "Inicio",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            BottomNavItem(
                icon = Icons.Outlined.Article,
                selectedIcon = Icons.Default.Article,
                label = "Reportes",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
            BottomNavItem(
                icon = Icons.Outlined.Settings,
                selectedIcon = Icons.Default.Settings,
                label = "Ajustes",
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) NavPillActive else Color.Transparent)
                .padding(horizontal = 20.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) selectedIcon else icon,
                contentDescription = label,
                tint = if (isSelected) HeaderBlue else TextMuted,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) HeaderBlue else TextMuted
        )
    }
}
