package io.github.almelaxavimarti.falls.modelo;

/** Cambio de compás en un tick, por ejemplo 4/4 o 7/8. */
public record CambioCompas(long tick, int numerador, int denominador) {

    public CambioCompas {
        if (tick < 0) {
            throw new IllegalArgumentException("Tick negativo: " + tick);
        }
        if (numerador < 1) {
            throw new IllegalArgumentException("Numerador debe ser al menos 1: " + numerador);
        }
        if (denominador < 1 || Integer.bitCount(denominador) != 1) {
            throw new IllegalArgumentException("Denominador debe ser potencia de 2 (1, 2, 4, 8...): " + denominador);
        }
    }
}
