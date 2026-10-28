# 003. `core` sin JavaFX ni javax.sound.midi

- **Estado:** Vigente
- **Fecha:** 2026-10-28

## Contexto
La lógica de Falls tiene que poder reutilizarse en otras versiones: servidor MCP (v2), Android y plugin (v3). Android no tiene JavaFX ni `javax.sound.midi`.

## Decisión
El módulo `core` es Java puro. La lectura y escritura de MIDI se define con interfaces propias; la implementación concreta va en un adaptador. La interfaz gráfica y la reproducción de sonido viven en `desktop`.

## Consecuencias
- `core` se puede probar sin ventanas ni sonido.
- Hay que escribir algo más de código (interfaces y adaptadores): es la arquitectura hexagonal.
