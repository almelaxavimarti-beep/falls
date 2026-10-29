package io.github.almelaxavimarti.falls.modelo;

import java.util.List;

/** Una canción completa, siempre con 960 ticks por negra (decisión 001). */
public record Cancion(List<CambioTempo> tempos, List<CambioCompas> compases,
                      List<CambioTonalidad> tonalidades, List<Pista> pistas) {

    public static final int TICKS_POR_NEGRA = 960;

    public Cancion {
        // TODO 1: guardar copias que no se puedan modificar de las cuatro listas (List.copyOf)
        // TODO 2: rechazar tempos, compases o tonalidades que no estén ordenados por tick.
        //         Pista: un for desde i = 1 que compare cada elemento con el anterior.
        //         Hay que hacerlo para las tres listas.
    }
}
