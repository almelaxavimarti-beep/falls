package io.github.almelaxavimarti.falls.modelo;

/**
 * Cambio de armadura en un tick, tal como lo guarda MIDI:
 * alteraciones negativas = bemoles, positivas = sostenidos; menor = true si es tonalidad menor.
 */
public record CambioTonalidad(long tick, int alteraciones, boolean menor) {

    public CambioTonalidad {
        // TODO 1: rechazar tick negativo
        // TODO 2: rechazar alteraciones fuera de -7 a 7
    }
}
