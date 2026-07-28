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

    // ── Campos generales existentes ──────────────────────────────────────────
    val title: String = "",
    val date: String = "",
    val technicianName: String = "",   // Técnico Responsable
    val location: String = "",
    val description: String = "",
    val observations: String = "",
    val signaturePath: String? = null,
    val photos: List<String> = emptyList(),
    val isDraft: Boolean = true
)
