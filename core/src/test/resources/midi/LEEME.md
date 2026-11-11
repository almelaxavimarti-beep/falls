# MIDIs de prueba de Falls

Generados el 28/10/2026 para probar el lector MIDI (Ciclo 1) y las reglas del corrector (Ciclos 3 y 5).
Cada archivo tiene errores colocados a propósito en posiciones conocidas: son la "respuesta correcta" con la que se comprueba si Falls detecta lo que debe.

Todos son 4/4, 8 compases. Posiciones en formato compás:tiempo, empezando en 1:1.
La melodía de los archivos 01, 02 y 05 es una canción infantil tradicional (dominio público). Los archivos son originales y se pueden subir al repositorio.

| Archivo | Tonalidad | Resolución | Tempo | Pistas | Errores esperados |
|---|---|---|---|---|---|
| `01_referencia_do_mayor.mid` | Do mayor | 960 | 120 | Melodía | **Ninguno.** Sirve para comprobar que Falls no da falsos avisos. |
| `02_notas_fuera_de_escala.mid` | Do mayor | 960 | 120 | Melodía | Nota fuera de escala en **2:1 (Mi♭4)**, **4:2 (Fa♯4)** y **7:3 (Sol♯4)**. |
| `03_choque_con_acorde.mid` | Do mayor | 960 | 100 | Melodía + Acordes (Do, Lam, Fa, Sol, ×2) | Choque con el acorde, en tiempo fuerte y nota larga: **1:1 Fa4 sobre Do** (semitono con Mi), **3:1 Si4 sobre Fa** (tritono con Fa, semitono con Do), **4:1 Do5 sobre Sol** (semitono con Si), **6:1 Si4 sobre Lam** (semitono con Do). Todas las notas están dentro de la escala: aquí no debe saltar la regla de fuera de escala. |
| `04_intervalos_asperos.mid` | Do mayor | 960 | 110 | Melodía | Saltos ásperos: **2:1→2:2 Fa4→Si4** (tritono), **3:3→3:4 Mi4→Fa5** (novena menor), **5:1→5:2 Do4→Si4** (séptima mayor), **6:2→6:3 Si3→Fa4** (tritono). |
| `05_velocidades_planas.mid` | Do mayor | 960 | 120 | Melodía | **Velocidades planas:** las 26 notas con velocidad 100. Misma melodía que 01. |
| `06_la_menor_96ppq_varios_errores.mid` | La menor | **96** (como FL Studio) | **120 → 90 en 5:1** | Melodía | Nota fuera de escala en **3:2 (Re♯5)**. **Velocidades planas** en los compases 5-8 (todas a 90). Sirve además para probar la conversión de 96 a 960 ticks por negra y el cambio de tempo. |

Comprobado: los 6 archivos se abren con `javax.sound.midi` de Java con la resolución, el número de pistas y la duración esperados.

Pendiente: exportar también 3 o 4 melodías reales hechas en FL Studio, porque los archivos de FL traen detalles (nombres de pista, canales, eventos extra) que estos sintéticos no tienen.
