# 012. Lector y escritor MIDI propios y completos

- **Estado:** Vigente
- **Fecha:** 2026-10-29

## Contexto
Falls necesita leer y escribir archivos MIDI en `core`, que no puede depender de `javax.sound.midi` (decisión 003). Además tiene que funcionar con melodías complejas de productores con experiencia, no solo con ejemplos sencillos. Las opciones eran usar una librería (android-midi-lib) o escribir el lector y el escritor desde cero.

## Decisión
Falls tiene su propio lector y escritor de MIDI, en Java puro dentro de `core`, detrás de las interfaces `LectorMidi` y `EscritorMidi`. Siguen la especificación *Standard MIDI File 1.0* y soportan como mínimo:

- Formatos 0 y 1 (el formato 0 se separa en pistas por canal; el 2 se rechaza con un error claro).
- Cualquier resolución de entrada, convertida a 960 ticks por negra (decisión 001).
- Polifonía en una misma pista, y la misma nota solapada consigo misma.
- Varios canales; el canal 10 se trata como batería.
- Cambios de tempo, de compás y de tonalidad (si el archivo los trae).
- Controladores (sustain, modulación...) y pitch bend.
- *Running status*, note-on con velocidad 0 como final de nota, notas sin final y eventos SysEx o desconocidos (se saltan sin romper la lectura).

Un archivo corrupto produce un error con la causa y la posición, nunca un cuelgue.

## Consecuencias
- Ninguna dependencia externa para lo más básico del proyecto, y funciona igual en Android.
- Más trabajo al principio, a cambio de aprender cómo es un formato binario por dentro.
- Los errores son nuestros: se controlan con los tests de la decisión 014.
- Si hiciera falta, se podría cambiar a una librería sin tocar el resto de `core`, porque todo pasa por las interfaces.
