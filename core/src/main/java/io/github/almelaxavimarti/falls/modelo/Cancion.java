package io.github.almelaxavimarti.falls.modelo;

import java.util.List;

/** Una canción completa, siempre con 960 ticks por negra (decisión 001). */
public record Cancion(List<CambioTempo> tempos, List<CambioCompas> compases,
                      List<CambioTonalidad> tonalidades, List<Pista> pistas) {

    public static final int TICKS_POR_NEGRA = 960;

    public Cancion {
        tempos = List.copyOf(tempos);
        compases = List.copyOf(compases);
        tonalidades = List.copyOf(tonalidades);
        pistas = List.copyOf(pistas);

        for (int i = 1; i < tempos.size(); i++) {
            if (tempos.get(i).tick() < tempos.get(i - 1).tick()) {
                throw new IllegalArgumentException("Los cambios de tempo no están ordenados por tick");
            }
        }
        for (int i = 1; i < compases.size(); i++) {
            if (compases.get(i).tick() < compases.get(i - 1).tick()) {
                throw new IllegalArgumentException("Los cambios de compás no están ordenados por tick");
            }
        }
        for (int i = 1; i < tonalidades.size(); i++) {
            if (tonalidades.get(i).tick() < tonalidades.get(i - 1).tick()) {
                throw new IllegalArgumentException("Los cambios de tonalidad no están ordenados por tick");
            }
        }
    }
}
