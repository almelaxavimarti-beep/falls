package io.github.almelaxavimarti.falls.modelo;

/** Cambio de tempo en un tick, en pulsaciones por minuto. */
public record CambioTempo(long tick, double bpm) {

    public CambioTempo {
        // TODO 1: rechazar tick negativo
        // TODO 2: rechazar bpm 0 o negativo
    }

    /** Crea un cambio de tempo a partir del formato MIDI: microsegundos que dura una negra. */
    public static CambioTempo desdeMicrosegundos(long tick, int microsegundosPorNegra) {
        // TODO 3: rechazar microsegundosPorNegra 0 o negativo
        // TODO 4: calcular los bpm (1 minuto = 60.000.000 microsegundos)
        return new CambioTempo(tick, 120.0);
    }

    /** El tempo en formato MIDI: microsegundos que dura una negra, redondeado. */
    public int microsegundosPorNegra() {
        return 0; // TODO 5: el cálculo al revés. Pista: Math.round
    }
}
