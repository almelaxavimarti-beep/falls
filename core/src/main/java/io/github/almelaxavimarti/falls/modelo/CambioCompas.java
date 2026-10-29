package io.github.almelaxavimarti.falls.modelo;

/** Cambio de compás en un tick, por ejemplo 4/4 o 7/8. */
public record CambioCompas(long tick, int numerador, int denominador) {

    public CambioCompas {
        // TODO 1: rechazar tick negativo
        // TODO 2: rechazar numerador menor que 1
        // TODO 3: rechazar denominador que no sea potencia de 2 (1, 2, 4, 8, 16...)
        //         Pista: Integer.bitCount(n) cuenta los bits a 1 de n
    }
}
