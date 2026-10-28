# 001. Tiempo en ticks, 960 por negra

- **Estado:** Vigente
- **Fecha:** 2026-10-28

## Contexto
Los MIDI guardan el tiempo en ticks, con una resolución que cambia de un archivo a otro (96, 480, 960...). Hay que comparar y analizar notas de archivos distintos.

## Decisión
Internamente el tiempo se mide en ticks con una resolución fija de 960 por negra. Al leer un archivo se convierte a esa resolución. Se guardan también el compás y los cambios de tempo.

## Consecuencias
- Las reglas trabajan siempre con la misma unidad.
- 960 divide bien entre 2, 3, 4, 5, 6 y 8, así que tresillos y semicorcheas caen en ticks enteros.
- Para hablar en segundos hay que pasar por el mapa de tempos.
