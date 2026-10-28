# 002. Nota como número MIDI; nombre según la tonalidad

- **Estado:** Vigente
- **Fecha:** 2026-10-28

## Contexto
Una misma tecla puede llamarse de dos formas (Do# o Reb). El nombre correcto depende de la tonalidad.

## Decisión
La altura de una nota se guarda como número MIDI (0-127). El nombre se calcula cuando hace falta mostrarlo, a partir de la tonalidad detectada.

## Consecuencias
- Comparar e intervalos son operaciones con enteros.
- Las explicaciones usan el nombre que corresponde a la tonalidad, no uno fijo.
