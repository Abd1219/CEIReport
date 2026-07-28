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

### 6. Pantalla 3: Actividades Realizadas y Observaciones **[NUEVO]**
- **Actividades Realizadas**: Registro dinámico de actividades ejecutadas durante la jornada con botones de agregar/eliminar, badges numerados y `AlertDialog` interactivo.
- **Observaciones y Notas de Campo**: Apartado independiente con mismo sistema de agregar/eliminar, diferenciado visualmente con color `AccentOrange`.
- Ambas listas se guardan como `List<String>` en la entidad `Report` y se exportan numeradas en Excel.

### 7. Pantalla 4: Fuerza de Trabajo **[NUEVO]**
- **Personal en Campo**: Tabla interactiva para registrar Cantidad y Horas trabajadas de 10 roles específicos (*Sp seg, Residente, O.P., Topógrafo, Cadenero, Oficiales, Ayudante, Banderero, Sup. Obra, Sup. Calidad*).
- **Totales Automáticos**: Cálculo dinámico en tiempo real de totales de personal y horas trabajadas.
- **Total de HH**: Banner destacado que calcula y muestra las Horas Hombre acumuladas de la jornada.

### 8. Pantalla 5: Maquinaria Utilizada **[NUEVO]**
- **Equipos en Campo**: Tabla interactiva para registrar Cantidad y Horas trabajadas de 7 equipos específicos (*Bailarina, Hormigonera, Minicar, Vehículos, Generador, Rotomartillo, Compresor*).
- **Totales Automáticos**: Cálculo en tiempo real del Total de Cantidad y Total de Horas de equipos.
- **Total de HM**: Banner destacado que calcula y muestra las Horas Máquina (HM) totales.

### 9. Pantalla 6: Evidencias Fotográficas, Firma y Finalización
- **Captura e Importación de Fotos**: Integración con cámara del dispositivo y galería de fotos.
- **Firma Digital**: Modal interactivo de firma (*SignaturePad*) con previsualización en tiempo real.
- **Exportación única a Excel (.xlsx)**: Generación automática de archivo **Excel (.xlsx)** (se eliminó el PDF) con opción de compartir mediante Intent nativo (*WhatsApp, Correo, Drive*).

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

- **[2026-07-28]** *(Sesión 4 - Maquinaria Utilizada & Navegación Deslizable)*:
  - Nueva **Pantalla 5**: Maquinaria Utilizada con tabla para 7 equipos (Bailarina, Hormigonera, Minicar, Vehículos, Generador, Rotomartillo, Compresor).
  - Cálculo de **Total de HM** (Horas Máquina) y total de equipos en tiempo real.
  - Implementada **Navegación Deslizable (HorizontalPager)**: los usuarios pueden cambiar entre los 6 pasos deslizando horizontalmente la pantalla hacia la izquierda o derecha.
  - Flujo de formulario expandido a **6 pasos**.
  - `ExcelGenerator` actualizado para renderizar la tabla de Maquinaria Utilizada y Total de HM.
  - Actualización de entidad `Report` y migración a Room DB v6.
- **[2026-07-28]** *(Sesión 3 - Fuerza de Trabajo)*:
  - Nueva **Pantalla 4**: Fuerza de Trabajo con tabla interactiva de 10 roles (Sp seg, Residente, O.P., Topógrafo, Cadenero, Oficiales, Ayudante, Banderero, Sup. Obra, Sup. Calidad).
  - Ingreso de **Cantidad** y **Horas** por cada rol con teclados numéricos adaptados.
  - Cálculo automático en tiempo real del Total de Cantidad, Total de Horas y banner de **"Total de HH"**.
  - Flujo de formulario expandido a **5 pasos**.
  - `ExcelGenerator` actualizado para incluir la tabla de Fuerza de Trabajo y Total HH.
  - Actualización de entidad `Report` y migración a Room DB v5.
- **[2026-07-28]** *(Sesión 2)*:
  - Nueva **Pantalla 3**: Actividades Realizadas y Observaciones con listas dinámicas de agregar/eliminar ítems.
  - Flujo de formulario actualizado de **3 a 4 pasos**.
  - Eliminación completa del generador de PDF (`PdfGenerator.kt`).
  - Exportación simplificada: únicamente **Excel (.xlsx)** al finalizar el reporte.
  - `ExcelGenerator` actualizado para renderizar `actividadesRealizadas` y `observacionesList` como ítems numerados.
  - Entidad `Report` ampliada con campos `actividadesRealizadas` y `observacionesList` (Room DB v4).
- **[2026-07-28]** *(Sesión 1)*:
  - Saludo dinámico según horario del día.
  - Eliminación de datos estáticos/placeholder de usuario.
  - Lógica dinámica de Proyecto Activo (continuación de borradores pendientes o estado inactivo).
  - Implementación de diálogo de confirmación para guardar borrador al retroceder en cualquier pantalla del formulario.
  - Verificación exitosa de compilación mediante `./gradlew compileDebugKotlin`.
  - Sincronización completa con repositorio en GitHub.

---
*Desarrollado por AbdApps*
