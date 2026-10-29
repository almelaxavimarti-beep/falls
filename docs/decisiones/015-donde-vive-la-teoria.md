# 015. Dónde vive la teoría musical

- **Estado:** Vigente
- **Fecha:** 2026-10-29

## Contexto
Falls necesita mucha teoría musical: escalas, modos, acordes, intervalos y recursos melódicos, además de las reglas que la aplican. Había que decidir dónde guardarla.

## Decisión
- **La teoría como datos:** archivos JSON en `core/src/main/resources/teoria/` (escalas, acordes, intervalos, recursos), con campo `version` (decisión 007).
- **Las reglas como código:** una clase Java por regla, con sus tests, que usa esos datos.
- **La documentación:** en Markdown, dentro del repositorio: `docs/teoria/` (la explicación de la teoría, con fuentes) y `docs/reglas/` (una ficha por regla).

La base de datos (SQLite, v2) se reserva para datos del usuario que cambian: progreso, favoritos, historial.

## Consecuencias
- Cada cambio en la teoría pasa por Git, con su PR y su revisión.
- Los tests cargan los mismos datos que usa Falls.
- No hay que mantener un motor de base de datos para algo que no cambia mientras se usa la aplicación.
