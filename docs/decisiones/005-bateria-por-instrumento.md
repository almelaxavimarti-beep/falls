# 005. Batería por instrumento, nota MIDI solo al exportar

- **Estado:** Vigente
- **Fecha:** 2026-10-28

## Contexto
Cada DAW y cada kit asignan los sonidos de batería a notas distintas.

## Decisión
Dentro de Falls la batería se describe por instrumento (KICK, SNARE, CLAP, HAT...). Solo al exportar se traduce a números de nota, con un mapa configurable (General MIDI por defecto).

## Consecuencias
- Las plantillas son legibles y no dependen del DAW.
- Hace falta mantener los mapas de exportación.
