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
