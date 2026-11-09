package io.github.almelaxavimarti.falls.midi;

import java.io.IOException;

/**
 * El archivo no se puede leer como MIDI: está cortado, dañado o usa algo que Falls no soporta.
 * Siempre indica en qué byte del archivo se encontró el problema (decisión 012).
 */
public class ArchivoMidiCorruptoException extends IOException {

    private final int posicion;

    public ArchivoMidiCorruptoException(String mensaje, int posicion) {
        super(mensaje + " (byte " + posicion + ")");
        this.posicion = posicion;
    }

    /** Byte del archivo (empezando en 0) donde se detectó el problema. */
    public int posicion() {
        return posicion;
    }
}
