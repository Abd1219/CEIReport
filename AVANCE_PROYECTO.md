# Avance del Proyecto - CEIReport

## 📱 Descripción General
**CEIReport** es una aplicación Android desarrollada en Kotlin con Jetpack Compose (Material 3) y Room Database para la generación, gestión, firma y exportación de reportes técnicos de obra y supervisión de campo.

---

## 🚀 Estado Actual e Implementación por Pantallas

### 1. Rediseño Completo de la Interfaz (UI/UX)
- **Sistema de Temas Personalizado**: Paleta vibrante basada en azul principal (`HeaderBlue`), naranja de acento (`AccentOrange`), tarjetas elevadas blancas y fondos suaves de aplicación.
- **Barra de Navegación Inferior (BottomBar)**: Pestañas de *Inicio*, *Reportes* y *Ajustes* con selector tipo píldora.
- **Indicador de Progreso por Pasos**: Componente visual (`StepProgressBar`) en la parte superior de cada formulario.

### 2. Dashboard Principal (Lista de Reportes)
- **Saludo Dinámico por Horario**: Cambio automático entre *"¡Buenos días! ☀️"*, *"¡Buenas tardes! 🌤️"* y *"¡Buenas noches! 🌙"* de acuerdo a la hora del dispositivo.
- **Tarjeta de Proyecto Activo Dinámica**:
  - Si existe un reporte inconcluso/borrador, muestra automáticamente el proyecto en curso con la insignia **"EN CURSO"** y permite continuar la edición inmediatamente al pulsar sobre la tarjeta.
  - Si no hay reportes pendientes, muestra *"No hay proyectos activos"* con la insignia **"SIN PROYECTO"**.
- **Tarjetas de Resumen Estadístico**: Indicadores clave en tiempo real (*Borradores*, *Completados*, *Pendientes*).
- **Banner Naranja "Nuevo Reporte"**: Acceso directo para iniciar un nuevo reporte diario de campo.
- **Lista de Reportes Recientes**: Tarjetas con identificador numérico (`CEI-2025-XXXX`), badge de estado (*Borrador* / *Enviado*), fecha y acciones de edición y eliminación.

### 3. Confirmación al Retroceder en Formularios
- **Diálogo de Confirmación (`ExitConfirmationDialog`)**: Al intentar regresar a la pantalla principal (ya sea mediante el botón superior de navegación o el botón/gesto nativo del dispositivo con `BackHandler`), se solicita confirmación al usuario para elegir entre:
  - *Guardar borrador*: Almacena los cambios realizados y regresa a la pantalla principal.
  - *Salir sin guardar*: Descarta los cambios no guardados.
  - *Cancelar*: Permite continuar editando el reporte.

### 4. Pantalla 1: Datos Generales del Proyecto
- Captura de metadatos de obra: **Proyecto**, **Fase**, **Área**, **Sistema**, **Disciplina**, **Responsable**, **Fecha** (con DatePicker dialog), **No. Contrato** y **Descripción de Alcance**.

### 5. Pantalla 2: Seguridad en Campo y Condiciones Climáticas
- **Actividades de Seguridad**: Registro dinámico de charlas de 5 min, permisos y observaciones de seguridad con numeración visual.
- **Condiciones Climáticas**: Matriz interactiva de selección de clima con chips coloridos e íconos (Soleado, Nublado, Lluvioso, Tormenta, Ventoso, Frío, etc.).

### 6. Pantalla 3: Evidencias Fotográficas, Firma y Finalización
- **Captura e Importación de Fotos**: Integración con cámara del dispositivo y galería de fotos.
- **Firma Digital**: Modal interactivo de firma (*SignaturePad*) con previsualización en tiempo real.
- **Exportación Dual**: Generación automática de archivos **PDF** y **Excel (.xlsx)** con opción de compartir mediante Intent nativo (*WhatsApp, Correo, Drive*).

### 7. Splash Screen
- Animación de entrada con logo, barra de progreso y branding *"Desarrollado por AbdApps"*.

---

## 🛠️ Tecnologías y Arquitectura

- **Lenguaje**: Kotlin
- **UI Framework**: Jetpack Compose con Material 3
- **Base de Datos Local**: Room Database v3 con Type Converters
- **Navegación**: State-driven Navigation + BackHandler nativo
- **Generación de Archivos**: Apache POI (Excel) + Canvas/PdfDocument (PDF)
- **Build System**: Gradle con Kotlin DSL y KSP

---

## 📅 Historial de Actualizaciones Recientes

- **[2026-07-28]**:
  - Saludo dinámico según horario del día.
  - Eliminación de datos estáticos/placeholder de usuario.
  - Lógica dinámica de Proyecto Activo (continuación de borradores pendientes o estado inactivo).
  - Implementación de diálogo de confirmación para guardar borrador al retroceder en cualquier pantalla del formulario.
  - Verificación exitosa de compilación mediante `./gradlew compileDebugKotlin`.
  - Sincronización completa con repositorio en GitHub.

---
*Desarrollado por AbdApps*
