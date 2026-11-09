package io.github.almelaxavimarti.falls.modelo;

/** Mensaje de controlador MIDI (pedal de sustain, modulación...). */
public record Controlador(long tick, int canal, int numero, int valor) {

    /**
     * Número de controlador del pedal de sustain.
     */
    public static final int SUSTAIN = 64;

    public Controlador {
        if (tick < 0) {
            throw new IllegalArgumentException("Tick negativo: " + tick);
        }
        if (canal < 0 || canal > 15) {
            throw new IllegalArgumentException("Canal fuera de rango (0-15): " + canal);
        }
        if (numero < 0 || numero > 127) {
            throw new IllegalArgumentException("Número de controlador fuera de rango (0-127): " + numero);
        }
        if (valor < 0 || valor > 127) {
            throw new IllegalArgumentException("Valor fuera de rango (0-127): " + valor);
        }
    }
}
