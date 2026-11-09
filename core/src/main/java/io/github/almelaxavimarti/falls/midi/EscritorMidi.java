package io.github.almelaxavimarti.falls.midi;

import io.github.almelaxavimarti.falls.modelo.Cancion;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Convierte una {@link Cancion} en un archivo MIDI (decisiones 003 y 012). */
public interface EscritorMidi {

    /** Devuelve los bytes del archivo MIDI que representa la canción. */
    byte[] escribir(Cancion cancion);

    /** Escribe la canción en un archivo del disco. */
    default void escribir(Cancion cancion, Path archivo) throws IOException {
        Files.write(archivo, escribir(cancion));
    }
}
