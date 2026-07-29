# 📱 CEIReport - Generador de Reportes Diarios de Obra

**CEIReport** es una aplicación Android nativa desarrollada en Kotlin y Jetpack Compose diseñada para la captura de campo y generación de reportes diarios de supervisión de obra en formato Excel (`.xlsx`).

---

## 🚀 Inicio Rápido para Agentes AI

Si eres una IA trabajando en este proyecto, consulta inmediatamente los siguientes archivos clave de contexto:

1. 🤖 **[AGENTS.md](AGENTS.md)**: Reglas críticas del entorno local (configuración de `JAVA_HOME` para JDK de Android Studio, comandos de compilación y convenciones de ejecución).
2. 📋 **[ARCHITECTURE.md](ARCHITECTURE.md)**: Mapa arquitectónico detallado del proyecto (Modelos de Datos, Navegación de 8 Pasos, Room DB, ViewModels y Generador de Excel).
3. 📈 **[AVANCE_PROYECTO.md](AVANCE_PROYECTO.md)**: Registro histórico completo de sesiones, funciones completadas y tareas pendientes.

---

## 🛠️ Stack Tecnológico

- **Lenguaje**: Kotlin 2.x
- **UI Framework**: Jetpack Compose con Material 3 (Soporte completo para Modo Claro y Modo Oscuro)
- **Base de Datos**: Room Database v3 (`ReportDatabase`, `ReportDao`, `Converters` para listas JSON)
- **Patrón de Arquitectura**: MVVM (`ReportViewModel`, `StateFlow`)
- **Navegación**: Flow interactivo basado en estado con `HorizontalPager` de 8 pasos y `BackHandler` nativo.
- **Motor de Excel**: Apache POI (XSSF) con diseño corporativo avanzado, tablas zebra, marcas de agua y firmas digitales.
- **Imágenes / Croquis / Cámara**: Integration con Activity Contracts, `FileProvider` y escalado dinámico de firmas 1:1 (`SignaturePad`).

---

## 📁 Estructura del Proyecto

```text
CEIReport/
├── app/src/main/java/com/abdapps/ceireport/
│   ├── MainActivity.kt                # Punto de entrada, Dashboard y FormPagerFlow
│   ├── data/
│   │   ├── model/
│   │   │   └── Report.kt              # Modelo de datos Room (Campos del reporte)
│   │   ├── local/
│   │   │   ├── ReportDao.kt           # Interfaces de consulta Room
│   │   │   ├── ReportDatabase.kt      # Instancia de Base de Datos
│   │   │   └── Converters.kt          # TypeConverters para List<String>
│   │   ├── repository/
│   │   │   └── ReportRepository.kt    # Repositorio de datos
│   │   └── excel/
│   │       └── ExcelGenerator.kt      # Motor de generación de reportes .xlsx profesional
│   └── ui/
│       ├── components/
│       │   └── SignaturePad.kt        # Componente de lienzo interactivo para firmas
│       ├── screens/
│       │   ├── SplashScreen.kt        # Pantalla de bienvenida con logo institucional
│       │   ├── ReportListScreen.kt    # Dashboard / Historial de reportes
│       │   ├── GeneralDataScreen.kt   # Paso 1: Identificación y datos generales
│       │   ├── SafetyWeatherScreen.kt # Paso 2: Seguridad y condiciones climáticas
│       │   ├── ActivitiesObservationsScreen.kt # Paso 3: Actividades realizadas y notas
│       │   ├── WorkforceScreen.kt     # Paso 4: Fuerza de trabajo y horas hombre
│       │   ├── MachineryScreen.kt     # Paso 5: Maquinaria y horas máquina
│       │   ├── PlannedActivitiesScreen.kt # Paso 6: Actividades planeadas (siguiente día)
│       │   ├── ReportFormScreen.kt    # Paso 7: Evidencias fotográficas y croquis
│       │   └── FinalizeScreen.kt      # Paso 8: Avance por área, firmas y exportación
│       ├── theme/                     # Sistema de colores, tipografías y tema Material 3
│       └── viewmodel/
│           └── ReportViewModel.kt     # Estado global y lógica de negocio
├── AGENTS.md                          # Guía de entorno para Agentes AI
├── ARCHITECTURE.md                    # Documentación técnica y mapa de componentes
└── AVANCE_PROYECTO.md                 # Bitácora de avance y estado de desarrollo
```

---

## ⚡ Comandos de Compilación (PowerShell)

> **IMPORTANTE**: La variable `JAVA_HOME` local requiere apuntar al JDK integrado de Android Studio para compilar sin errores.

```powershell
# Compilar proyecto en busca de errores Kotlin
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat compileDebugKotlin

# Construir APK de prueba (Debug)
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat assembleDebug
```
