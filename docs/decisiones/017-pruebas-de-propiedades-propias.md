# 017. Tests de propiedades con un generador propio

- **Estado:** Vigente
- **Fecha:** 2026-11-27
- **Cambia:** la parte de jqwik de la decisión 014

## Contexto
La decisión 014 pedía tests de propiedades con jqwik: generar canciones al azar y comprobar que escribir y volver a leer da lo mismo. En 2026 el autor de jqwik añadió a su guía una cláusula pidiendo que no se use con agentes de IA que escriben código. No es un cambio de licencia, pero sí un deseo explícito del autor, y en Falls los tests los prepara Claude.

## Decisión
Los tests de propiedades se hacen con JUnit y un generador propio (`GeneradorCanciones`, con `java.util.Random` y semilla fija), sin jqwik:
- Cada test recorre cientos de semillas. Con la misma semilla sale siempre la misma canción, así que un fallo se puede repetir.
- El mensaje de cada fallo dice la semilla.
- Cuando un test de propiedades encuentra un fallo, se añade también un test escrito a mano con ese caso (`ArchivosEstropeadosTest`).

## Consecuencias
- Ninguna dependencia nueva, y se ve cómo funciona por dentro.
- No hay "shrinking" (reducir el fallo al caso mínimo de forma automática): el caso mínimo se busca a mano al escribir el test de regresión.
- El generador hay que mantenerlo cuando cambie el modelo.
