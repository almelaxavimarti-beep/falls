# 011. Gradle como herramienta de construcción

- **Estado:** Vigente
- **Fecha:** 2026-10-28

## Contexto
Hacía falta elegir entre Maven y Gradle para compilar, gestionar librerías y pasar tests en un proyecto multimódulo.

## Decisión
Gradle, con wrapper (versión 9.8.2) y scripts en Kotlin (`build.gradle.kts`).

## Consecuencias
- La versión para Android (v3) lo exige, así que no habrá que migrar.
- El proyecto multimódulo queda más limpio que con Maven.
- Gradle no se ve en clase de momento; los conceptos (dependencias, ciclo de compilación, tests) son los mismos que en Maven.
