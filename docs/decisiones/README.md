# Registro de decisiones

Cada decisión importante del proyecto tiene un número y un archivo. Las decisiones no se borran: si una cambia, se escribe una nueva que la sustituye y la antigua se marca como **Sustituida**.

| N.º | Decisión | Estado |
|---|---|---|
| [001](001-tiempo-en-ticks.md) | Tiempo en ticks, 960 por negra | Vigente |
| [002](002-nota-numero-midi.md) | Nota como número MIDI; nombre según la tonalidad | Vigente |
| [003](003-core-independiente.md) | `core` sin JavaFX ni javax.sound.midi | Vigente |
| [004](004-rol-de-pista.md) | Rol de pista deducido y corregible | Vigente |
| [005](005-bateria-por-instrumento.md) | Batería por instrumento, nota MIDI solo al exportar | Vigente |
| [006](006-textos-fuera-del-codigo.md) | Textos fuera del código, ES/EN | Vigente |
| [007](007-json-con-version.md) | Todos los JSON con campo `version` | Vigente |
| [008](008-fachada-serializable.md) | Fachada serializable y generador determinista | Vigente |
| [009](009-licencia-mit.md) | Licencia MIT desde el primer commit | Vigente |
| [010](010-organizacion-y-prioridades.md) | Organización del trabajo y prioridad de los estudios | Vigente |
| [011](011-gradle.md) | Gradle como herramienta de construcción | Vigente |
| [012](012-lector-midi-propio.md) | Lector y escritor MIDI propios y completos | Vigente |
| [013](013-analisis-con-contexto.md) | Análisis con contexto: conflicto, recurso o sugerencia | Vigente |
| [014](014-estandar-de-calidad.md) | Estándar de calidad | Vigente |
| [015](015-donde-vive-la-teoria.md) | Teoría como datos, reglas como código, documentación en el repositorio | Vigente |
| [016](016-sin-servidor.md) | Sin servidor | Vigente |

Plantilla para una decisión nueva:

```markdown
# NNN. Título

- **Estado:** Vigente
- **Fecha:** AAAA-MM-DD

## Contexto
Qué problema hay y qué opciones se han considerado.

## Decisión
Qué se ha decidido.

## Consecuencias
Qué implica, a favor y en contra.
```
