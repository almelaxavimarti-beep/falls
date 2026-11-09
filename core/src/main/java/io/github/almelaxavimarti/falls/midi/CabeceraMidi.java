package io.github.almelaxavimarti.falls.midi;

/**
 * La cabecera de un archivo MIDI (bloque "MThd"): formato, número de pistas y resolución.
 */
public record CabeceraMidi(int formato, int numeroPistas, int ticksPorNegra) {

    /** Lee la cabecera desde el principio del archivo. */
    public static CabeceraMidi leer(LectorBytes lector) throws ArchivoMidiCorruptoException {
        int inicio = lector.posicion();
        if (lector.restantes() < 4 || !lector.leerTexto(4).equals("MThd")) {
            throw new ArchivoMidiCorruptoException("No es un archivo MIDI: no empieza por MThd", inicio);
        }

        int posLongitud = lector.posicion();
        long longitud = lector.leerUint32();
        if (longitud < 6) {
            throw new ArchivoMidiCorruptoException("Cabecera demasiado corta: " + longitud + " bytes", posLongitud);
        }

        int posFormato = lector.posicion();
        int formato = lector.leerUint16();
        if (formato != 0 && formato != 1) {
            throw new ArchivoMidiCorruptoException("Falls no soporta el formato " + formato, posFormato);
        }

        int posPistas = lector.posicion();
        int numeroPistas = lector.leerUint16();
        if (numeroPistas < 1) {
            throw new ArchivoMidiCorruptoException("El archivo no tiene pistas", posPistas);
        }

        int posDivision = lector.posicion();
        int division = lector.leerUint16();
        if ((division & 0x8000) != 0) {
            throw new ArchivoMidiCorruptoException("Falls no soporta el tiempo SMPTE", posDivision);
        }
        if (division == 0) {
            throw new ArchivoMidiCorruptoException("Cero ticks por negra", posDivision);
        }

        lector.saltar((int) (longitud - 6));
        return new CabeceraMidi(formato, numeroPistas, division);
    }
}