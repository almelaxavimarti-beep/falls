package io.github.almelaxavimarti.falls.midi;

import io.github.almelaxavimarti.falls.modelo.Cancion;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Convierte un archivo MIDI en una {@link Cancion} (decisiones 003 y 012). */
public interface LectorMidi {

    /** Lee una canción a partir de los bytes de un archivo MIDI. */
    Cancion leer(byte[] datos) throws ArchivoMidiCorruptoException;

    /** Lee una canción desde un archivo del disco. */
    default Cancion leer(Path archivo) throws IOException {
        return leer(Files.readAllBytes(archivo));
    }
}
