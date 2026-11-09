package io.github.almelaxavimarti.falls.midi;

import io.github.almelaxavimarti.falls.modelo.CambioCompas;
import io.github.almelaxavimarti.falls.modelo.CambioTempo;
import io.github.almelaxavimarti.falls.modelo.CambioTonalidad;
import io.github.almelaxavimarti.falls.modelo.Pista;
import java.util.List;

/**
 * Resultado de convertir los eventos de una pista MIDI: la pista con sus notas y los cambios
 * de tempo, compás y tonalidad que traía (normalmente están en la primera pista del archivo).
 * Todos los ticks ya están en la resolución de Falls (960 por negra).
 */
public record PistaConvertida(Pista pista, List<CambioTempo> tempos, List<CambioCompas> compases,
                              List<CambioTonalidad> tonalidades) {

    public PistaConvertida {
        tempos = List.copyOf(tempos);
        compases = List.copyOf(compases);
        tonalidades = List.copyOf(tonalidades);
    }
}
