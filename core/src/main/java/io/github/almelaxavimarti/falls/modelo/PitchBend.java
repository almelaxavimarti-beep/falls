package io.github.almelaxavimarti.falls.modelo;

/** Pitch bend: 0 = abajo del todo, CENTRO = sin bend, 16383 = arriba del todo. */
public record PitchBend(long tick, int canal, int valor) {

    public static final int CENTRO = 8192;

    public PitchBend {
        if (tick < 0) {
            throw new IllegalArgumentException("Tick negativo: " + tick);
        }
        if (canal < 0 || canal > 15) {
            throw new IllegalArgumentException("Canal fuera de rango (0-15): " + canal);
        }
        if (valor < 0 || valor > 16383) {
            throw new IllegalArgumentException("Pitch bend fuera de rango (0-16383): " + valor);
        }
    }
    }