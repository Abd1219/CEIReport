# Documento 00 - Análisis del Formato Oficial

## Proyecto

Reporte Diario CEI - Android

Versión: 1.0

# Objetivo

Analizar el formato oficial del Reporte Diario CEI para documentar su
estructura, funcionamiento y reglas de negocio.

# Información del archivo

-   Tipo: Microsoft Excel (.xlsx)
-   Hojas: INFO, FOTO

# Descripción

## INFO

Contiene la información administrativa y técnica del reporte.

## FOTO

Contiene las evidencias fotográficas y referencia datos de INFO.

# Flujo

Usuario → Formulario Android → Validaciones → Modelo de datos → Excel →
PDF → Compartir

# Reglas

1.  No modificar el diseño del formato.
2.  Respetar colores y logotipos.
3.  Generar Excel compatible.
4.  Generar PDF con el mismo diseño.
5.  Mantener referencias de la hoja FOTO.

# Restricciones

La aplicación solo automatiza el llenado del formato.

# Observaciones

La plantilla Excel nunca se modifica; siempre se trabaja sobre una
copia.
