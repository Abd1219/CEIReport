# Avance del Proyecto - CEIReport

## 📱 Descripción General
**CEIReport** es una aplicación Android desarrollada en Kotlin con Jetpack Compose y Room Database para la generación, gestión y control de reportes técnicos de obra y supervisión de campo.

---

## 🚀 Estado Actual e Implementación por Pantallas

### 1. Splash Screen (Pantalla de Bienvenida)
- **Animaciones**: Transición suave de escala y transparencia al iniciar la aplicación.
- **Indicador**: Barra de progreso lineal indicando carga de datos.
- **Branding**: Marca de desarrollador *"Desarrollado por AbdApps"* integrada en el pie de página.

### 2. Pantalla 1: Datos Generales del Proyecto
Modulo para la captura de los metadatos principales del contrato y la obra:
- **Proyecto** (Nombre del proyecto)
- **Fase** (Fase actual de ejecución)
- **Área** (Área geográfica o sector)
- **Sistema** (Sistema involucrado)
- **Disciplina** (Civil, Electromecánica, Instrumental, etc.)
- **Técnico Responsable** (Responsable en sitio)
- **Fecha** (Fecha del reporte)
- **No. Contrato** (Número de contrato de referencia)
- **Descripción de Alcance** (Detalle de los alcances del contrato)

### 3. Pantalla 2: Seguridad en Campo y Condiciones Climáticas
Modulo enfocado en el control de seguridad y ambiente operacional:
- **Actividades de Seguridad**: Formulario dinámico que permite agregar múltiples actividades/charlas/permisos de seguridad realizados durante la jornada.
- **Condiciones Climáticas**: Selector interactivo de clima en campo mediante tarjetas visuales con iconos (Soleado, Nublado, Lluvioso, Tormenta, Viento).

### 4. Flujo de Navegación y Gestión de Estado
- **Flujo de Pantallas**: `Splash` ➔ `Lista de Reportes` ➔ `Datos Generales (P1)` ➔ `Seguridad y Clima (P2)` ➔ `Formulario Principal`.
- **Persistencia**: Base de datos local **Room** (versión 3) con soporte para almacenamiento de colecciones mediante convertidores de datos (`Converters`).

---

## 🛠️ Tecnologías y Arquitectura

- **Lenguaje**: Kotlin
- **UI Framework**: Jetpack Compose con Material 3
- **Base de Datos Local**: Room Database
- **Navegación**: State-driven Navigation con Jetpack Compose
- **Build System**: Gradle con Kotlin DSL y KSP

---

## 📋 Próximos Pasos

1. **Pantalla 3: Evidencias Fotográficas**
   - Captura de fotos desde cámara o galería.
   - Asociación de comentarios/descripciones por cada fotografía.
2. **Pantalla 4: Firma y Finalización**
   - Captura de firma digital de los responsables.
   - Vista previa del reporte completo.
3. **Exportación y Generación de PDF**
   - Generación del documento final en formato PDF con la maqueta y formato estandarizado.

---
*Desarrollado por AbdApps*
