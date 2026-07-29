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

### 9. Pantalla 6: Actividades Planeadas para el Siguiente Día **[NUEVO]**
- **Registro Dinámico**: Lista interactiva para programar y planear las actividades de la siguiente jornada laboral con botón de agregar, diálogo interactivo y opción de eliminación.
- **Exportación Excel**: Se exporta numerada como sección dedicada en el archivo Excel generado.

### 10. Pantalla 7: Evidencias Fotográficas, Croquis Descriptivo y Firma del Contratista
- **Corrección de Cámara**: Solucionado el fallo al abrir la cámara agregando gestión de permisos en tiempo real (`Manifest.permission.CAMERA`) y `FileProvider` con rutas ampliadas.
- **Pie de Foto / Descripción de Fotografía**: Al tomar una foto o elegirla de la galería, se solicita un texto o nota descriptiva opcional que se muestra debajo de cada foto y se incluye en el reporte Excel.
- **Croquis Descriptivo**: Apartado dedicado para adjuntar o capturar una imagen del croquis o esquema del área de trabajo.
- **Firma Digital**: Modal interactivo de firma (*SignaturePad*) con previsualización en tiempo real.
- **Exportación única a Excel (.xlsx)**: Generación automática del archivo Excel con fotos, notas, croquis, actividades y firma.

### 11. Pantalla 8: Avance por Área, Supervisor y Finalización **[NUEVO]**
- **Tabla de Avance por Disciplina/Área**: Registro dinámico con filas de Área y % de Avance (0-100). Botón para agregar nuevas filas y eliminar existentes.
- **Firma del Supervisor**: Modal interactivo de firma digital (*SignaturePad*) para el supervisor de obra, independiente de la firma del contratista.
- **Nombre del Supervisor**: Campo de texto para registrar el nombre del supervisor responsable de validar el reporte.
- **Exportación Excel**: Sección dedicada en el archivo `.xlsx` con la tabla de avances y firma del supervisor.
- **Flujo expandido a 8 pasos**: El formulario ahora incluye esta pantalla como paso final antes de exportar.
- Entidad `Report` actualizada con campos `areasAvance`, `avancePorcentajes`, `supervisor` y `supervisorSignaturePath` (Room DB v8 pendiente).

### 12. Splash Screen
- Animación de entrada con **logo real `logocei.png`** del proyecto CEI (sustituye el ícono genérico), barra de progreso y branding *"Desarrollado por AbdApps"*.
- Fondo del contenedor del logo actualizado a azul corporativo (`#132B66`) con borde sutil.

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

- **[2026-07-28]** *(Sesión 6 - Pantalla Finalización, Avance por Área, Firma del Supervisor y Logo Real en SplashScreen)*:
  - Nueva **Pantalla 8 (FinalizeScreen)**: Captura del avance de obra por área/disciplina con porcentaje (0-100%), nombre del supervisor y su firma digital independiente.
  - **Flujo expandido a 8 pasos**: El `HorizontalPager` ahora gestiona 8 pantallas; `FinalizeScreen` se integra como paso final.
  - **Logo real en SplashScreen**: Sustituido el ícono genérico `FlashOn` por el logo oficial `logocei.png` con fondo azul corporativo y borde sutil.
  - **Corrección de navegación Back**: El botón de retroceso en todas las pantallas intermedias (Pasos 2-8) ahora invoca `onExitFlow()` directamente, permitiendo regresar al Dashboard sin reiniciar el flujo.
  - Entidad `Report` ampliada con `areasAvance: List<String>`, `avancePorcentajes: List<String>`, `supervisor: String` y `supervisorSignaturePath: String?`.
  - `ReportViewModel` actualizado con los setters para los nuevos campos de la pantalla de finalización.
  - `AGENTS.md` creado con configuración de entorno para compilación Gradle con JDK de Android Studio.
- **[2026-07-28]** *(Sesión 5 - Actividades Planeadas, Croquis & Solución de Cámara)*:
  - Nueva **Pantalla 6**: Actividades Planeadas para el Siguiente Día con lista dinámica interactiva.
  - **Corrección de error de Cámara**: Implementada solicitud de permisos `CAMERA` en tiempo real y `FileProvider` configurado con todas las rutas necesarias.
  - **Descripción / Pie de Foto**: Modal interactivo para agregar o editar notas explicativas por cada fotografía capturada o seleccionada.
  - **Croquis Descriptivo**: Sección dedicada en evidencias para adjuntar el croquis o esquema descriptivo del área.
  - Flujo expandido a **7 pasos** mantenidos con navegación deslizable (*HorizontalPager*).
  - Actualización de entidad `Report` y migración a Room DB v7.
  - `ExcelGenerator` actualizado para incluir Actividades Planeadas, notas al pie de imágenes y renderizado de Croquis Descriptivo.
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
