# 008. Fachada serializable y generador determinista

- **Estado:** Vigente
- **Fecha:** 2026-10-28

## Contexto
La misma lógica la usarán la aplicación de escritorio, los tests y, más adelante, un servidor MCP para IA.

## Decisión
`core` ofrece una fachada cuyos resultados son datos serializables a JSON. El generador de baterías es determinista: la misma semilla produce siempre el mismo patrón.

## Consecuencias
- Los tests son reproducibles.
- Un patrón se puede guardar o compartir solo con su semilla y sus parámetros.
