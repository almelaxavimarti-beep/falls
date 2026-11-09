package io.github.almelaxavimarti.falls.midi;

/**
 * La cabecera de un archivo MIDI (bloque "MThd"): formato, número de pistas y resolución.
 */
public record CabeceraMidi(int formato, int numeroPistas, int ticksPorNegra) {

    /** Lee la cabecera desde el principio del archivo. */
    public static CabeceraMidi leer(LectorBytes lector) throws ArchivoMidiCorruptoException {
        // TODO: ver la explicación en el chat
        return new CabeceraMidi(0, 0, 0);
    }
}
