package io.github.almelaxavimarti.falls.modelo;

/**
 * Cambio de armadura en un tick, tal como lo guarda MIDI:
 * alteraciones negativas = bemoles, positivas = sostenidos; menor = true si es tonalidad menor.
 */
public record CambioTonalidad(long tick, int alteraciones, boolean menor) {

    public CambioTonalidad {
        if (tick < 0) {
            throw new IllegalArgumentException("Tick negativo: " + tick);
        }
        if (alteraciones < -7 || alteraciones > 7) {
            throw new IllegalArgumentException("Alteraciones fuera de rango (-7 a 7): " + alteraciones);
        }
    }
}
