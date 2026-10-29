package io.github.almelaxavimarti.falls.modelo;

/** Mensaje de controlador MIDI (pedal de sustain, modulación...). */
public record Controlador(long tick, int canal, int numero, int valor) {

    /** Número de controlador del pedal de sustain. */
    public static final int SUSTAIN = 64;

    public Controlador {
        // TODO 1: rechazar tick negativo
        // TODO 2: rechazar canal fuera de 0-15
        // TODO 3: rechazar numero fuera de 0-127
        // TODO 4: rechazar valor fuera de 0-127
    }
}
