# 014. Estándar de calidad

- **Estado:** Vigente
- **Fecha:** 2026-10-29

## Contexto
Falls quiere ser una herramienta en la que se pueda confiar. No se puede evitar que a alguien no le guste algo, pero sí que nadie pueda demostrar que Falls se equivoca. Para eso la calidad tiene que ser medible.

## Decisión
- **Lector MIDI:** probado con archivos de varios DAW (FL Studio, Ableton, Reaper, MuseScore...), con tests de propiedades (jqwik) que generan canciones aleatorias y comprueban que escribir y volver a leer da lo mismo, y con archivos corruptos que deben dar un error claro.
- **Análisis:** un corpus de melodías anotadas, de sencillas a complejas, con el resultado correcto de cada una. Objetivo: ningún falso aviso en el corpus. Cada aviso apunta a un hecho comprobable.
- **Reglas:** una ficha por regla en `docs/reglas/`, con su base teórica y sus fuentes.
- **Código:** tests primero en `core` y cobertura mínima del 90 % medida con JaCoCo. La CI bloquea el PR si falla un test o baja la cobertura.
- **Antes de la v1:** beta con productores reales y, si es posible, revisión de alguien con formación en armonía.

## Consecuencias
- La v1 tarda más (la estimación pasa de junio a finales de 2027).
- El corpus es trabajo continuo y hay que mantenerlo.
- Se prioriza la calidad de lo que hay frente a añadir funciones.
