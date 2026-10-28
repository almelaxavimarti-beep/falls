# Falls

Herramienta gratuita y de código abierto para productores musicales, con dos partes:

1. **Corrector explicativo de melodías.** Lee el MIDI que has hecho, avisa de lo que no encaja (notas fuera de escala, choques con el acorde, intervalos ásperos, velocidades planas), propone una corrección y explica el mecanismo que hay detrás.
2. **Generador de baterías.** Patrones por género, con plantillas y variaciones controlables, listos para arrastrar a tu DAW.

Falls explica, no impone: distingue entre *conflictos* y *sugerencias de estilo*, y cada explicación se apoya en un hecho medible del archivo, nunca en un juicio estético.

> **Estado:** en desarrollo, todavía sin versión usable. Ahora mismo: Ciclo 0 (estructura base del proyecto).

## Requisitos

- Java 25 (por ejemplo, [Eclipse Temurin](https://adoptium.net/)).
- No hace falta instalar Gradle: el proyecto incluye el wrapper.

## Compilar y ejecutar

```bash
# Compilar y pasar los tests
./gradlew build          # en Windows: .\gradlew.bat build

# Ejecutar la aplicación de escritorio
./gradlew :desktop:run   # en Windows: .\gradlew.bat :desktop:run
```

## Estructura

| Módulo | Qué contiene |
|---|---|
| `core` | El núcleo en Java puro: modelo musical, lectura y escritura MIDI, análisis, corrector y generador. No depende de ninguna interfaz gráfica. |
| `desktop` | La aplicación de escritorio, que usa `core`. |

## Documentación

- [Registro de decisiones](docs/decisiones/README.md): por qué el proyecto está hecho como está.
- [CHANGELOG](CHANGELOG.md): cambios de cada versión.

## Licencia

[MIT](LICENSE) © Xavi Almela Marti
