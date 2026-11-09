# Changelog

Todos los cambios importantes de Falls se apuntan aquí.
Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/); versiones según [SemVer](https://semver.org/lang/es/).

## [Sin publicar]

### Añadido
- Estructura Gradle multimódulo (`core` y `desktop`) con wrapper de Gradle 9.8.2, Java 25 y JUnit 5.
- README con descripción, requisitos y cómo compilar.
- Registro de decisiones en `docs/decisiones/` (001-011).
- Integración continua con GitHub Actions: compila y pasa los tests en cada PR.
- Decisiones 012-016: lector MIDI propio, análisis con contexto, estándar de calidad, dónde vive la teoría y sin servidor.
- Modelo musical en `core`: `Nota`, `Pista`, `Cancion` y eventos (tempo, compás, tonalidad, controladores, pitch bend), con validaciones y listas inmutables. Cobertura de tests medida con JaCoCo (mínimo 90 %).
- Primera parte del lector MIDI propio (decisión 012): interfaces `LectorMidi` y `EscritorMidi`, `LectorBytes` (números big-endian, texto ASCII, cantidades de longitud variable) y lectura de la cabecera `MThd` con errores que indican el byte del problema.