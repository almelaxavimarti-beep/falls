package io.github.almelaxavimarti.falls.modelo;

/** Pitch bend: 0 = abajo del todo, CENTRO = sin bend, 16383 = arriba del todo. */
public record PitchBend(long tick, int canal, int valor) {

    public static final int CENTRO = 8192;

    public PitchBend {
        // TODO 1: rechazar tick negativo
        // TODO 2: rechazar canal fuera de 0-15
        // TODO 3: rechazar valor fuera de 0-16383
    }
}
