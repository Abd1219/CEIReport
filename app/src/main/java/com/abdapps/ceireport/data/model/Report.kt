package com.abdapps.ceireport.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // ── Pantalla 1: Datos Generales ──────────────────────────────────────────
    @ColumnInfo(defaultValue = "")
    val proyecto: String = "",
    @ColumnInfo(defaultValue = "")
    val fase: String = "",
    @ColumnInfo(defaultValue = "")
    val area: String = "",
    @ColumnInfo(defaultValue = "")
    val sistema: String = "",
    @ColumnInfo(defaultValue = "")
    val disciplina: String = "",
    @ColumnInfo(defaultValue = "")
    val noContrato: String = "",
    @ColumnInfo(defaultValue = "")
    val descripcionAlcance: String = "",

    // ── Pantalla 2: Seguridad y Clima ───────────────────────────────────────
    @ColumnInfo(defaultValue = "")
    val actividadesSeguridad: List<String> = emptyList(),
    @ColumnInfo(defaultValue = "")
    val clima: List<String> = emptyList(),

    // ── Pantalla 3: Actividades Realizadas y Observaciones ───────────────────
    @ColumnInfo(defaultValue = "")
    val actividadesRealizadas: List<String> = emptyList(),
    @ColumnInfo(defaultValue = "")
    val observacionesList: List<String> = emptyList(),

    // ── Pantalla 4: Fuerza de Trabajo ───────────────────────────────────────
    @ColumnInfo(defaultValue = "")
    val fuerzaTrabajoCantidades: List<String> = emptyList(), // 10 valores, uno por rol
    @ColumnInfo(defaultValue = "")
    val fuerzaTrabajoHoras: List<String> = emptyList(),     // 10 valores, uno por rol

    // ── Pantalla 5: Maquinaria Utilizada ───────────────────────────────────
    @ColumnInfo(defaultValue = "")
    val maquinariaCantidades: List<String> = emptyList(),   // 7 valores, uno por equipo
    @ColumnInfo(defaultValue = "")
    val maquinariaHoras: List<String> = emptyList(),       // 7 valores, uno por equipo

    // ── Pantalla 6: Actividades Planeadas (Siguiente Día) ─────────────────
    @ColumnInfo(defaultValue = "")
    val actividadesPlaneadas: List<String> = emptyList(),

    // ── Pantalla 7: Evidencias Fotográficas, Croquis y Firma ──────────────
    @ColumnInfo(defaultValue = "")
    val photoCaptions: List<String> = emptyList(),          // Descripción por cada foto en photos
    val croquisPath: String? = null,                        // Ruta de imagen de Croquis Descriptivo

    // ── Pantalla 8: Avance, Supervisor y Finalización ─────────────────────
    @ColumnInfo(defaultValue = "")
    val areasAvance: List<String> = emptyList(),            // Nombre de área/disciplina
    @ColumnInfo(defaultValue = "")
    val avancePorcentajes: List<String> = emptyList(),      // % de avance por área (0-100)
    @ColumnInfo(defaultValue = "")
    val supervisor: String = "",                            // Nombre del Supervisor
    val supervisorSignaturePath: String? = null,           // Ruta de imagen de Firma del Supervisor

    // ── Campos generales existentes ──────────────────────────────────────────
    val title: String = "",
    val date: String = "",
    val technicianName: String = "",   // Responsable de Contratista
    val location: String = "",
    val description: String = "",
    val observations: String = "",
    val signaturePath: String? = null,
    val photos: List<String> = emptyList(),
    val isDraft: Boolean = true
)
