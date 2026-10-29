# 013. Análisis con contexto

- **Estado:** Vigente
- **Fecha:** 2026-10-29

## Contexto
Falls es para principiantes y también para productores con experiencia. La música compleja usa a propósito notas fuera de la escala o del acorde: notas de paso cromáticas, apoyaturas, *blue notes*, préstamo modal, modos. Si Falls las marca como errores, pierde la confianza de quien sabe.

## Decisión
Cada regla clasifica lo que encuentra en uno de tres tipos:

1. **Conflicto:** algo que choca de forma objetiva (por ejemplo, una nota larga en tiempo fuerte a un semitono de una nota del acorde).
2. **Recurso:** un uso reconocido de la tensión, que Falls nombra y explica sin marcarlo como error (nota de paso, bordadura, apoyatura, escapada, *blue note*...).
3. **Sugerencia:** algo correcto que se podría hacer de otra manera, solo como idea.

La tonalidad se detecta por secciones, para soportar modulaciones, y la escala de referencia puede ser un modo (dórico, frigio, menor armónica...), no solo mayor o menor.

## Consecuencias
- Cada regla tiene que mirar el contexto de cada nota: notas vecinas, posición en el compás, duración y acorde que suena.
- Las reglas son más difíciles de escribir y de probar, y por eso necesitan el corpus de la decisión 014.
- Refuerza los principios "explica, no impone" y "solo hechos medibles".
