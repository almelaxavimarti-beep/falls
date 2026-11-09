package io.github.almelaxavimarti.falls.modelo;

/** Una nota: altura MIDI, inicio y duración en ticks, velocidad y canal. */
public record Nota(int altura, long inicio, long duracion, int velocidad, int canal) {

    public Nota {
        if (altura < 0 || altura > 127) {
            throw new IllegalArgumentException("Altura fuera de rango (0-127): " + altura);
        }
        if (inicio < 0) {
            throw new IllegalArgumentException("Inicio negativo: " + inicio);
        }
        if (duracion <= 0) {
            throw new IllegalArgumentException("Duración debe ser mayor que 0: " + duracion);
        }
        if (velocidad < 1 || velocidad > 127) {
            throw new IllegalArgumentException("Velocidad fuera de rango (1-127): " + velocidad);
        }
        if (canal < 0 || canal > 15) {
            throw new IllegalArgumentException("Canal fuera de rango (0-15): " + canal);
        }
    }

    /** Tick en el que termina la nota. */
    public long fin() {
        return inicio + duracion;
    }
}
