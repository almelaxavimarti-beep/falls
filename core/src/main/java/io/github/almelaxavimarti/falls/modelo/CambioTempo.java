package io.github.almelaxavimarti.falls.modelo;

/** Cambio de tempo en un tick, en pulsaciones por minuto. */
public record CambioTempo(long tick, double bpm) {

    public CambioTempo {
        if (tick < 0) {
            throw new IllegalArgumentException("Tick negativo: " + tick);
        }
        if (bpm <= 0) {
            throw new IllegalArgumentException("El tempo debe ser mayor que 0: " + bpm);
        }
    }

    /**
     * Crea un cambio de tempo a partir del formato MIDI: microsegundos que dura una negra.
     */
    public static CambioTempo desdeMicrosegundos(long tick, int microsegundosPorNegra) {
        if (microsegundosPorNegra <= 0) {
            throw new IllegalArgumentException("Microsegundos por negra debe ser mayor que 0: " + microsegundosPorNegra);
        }
        double bpm = 60_000_000.0 / microsegundosPorNegra;
        return new CambioTempo(tick, bpm);
    }

    /**
     * El tempo en formato MIDI: microsegundos que dura una negra, redondeado.
     */
    public int microsegundosPorNegra() {
        return (int) Math.round(60_000_000.0 / bpm);
    }
}