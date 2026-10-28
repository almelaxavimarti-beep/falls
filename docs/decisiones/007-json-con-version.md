# 007. Todos los JSON con campo `version`

- **Estado:** Vigente
- **Fecha:** 2026-10-28

## Contexto
Las plantillas, los resultados y los datos guardados usarán JSON, y su formato cambiará con el tiempo.

## Decisión
Todo JSON que escriba o lea Falls lleva un campo `version` con la versión de su formato.

## Consecuencias
- Se pueden migrar datos antiguos en vez de romperlos.
- La versión del formato es independiente de la versión de la aplicación.
