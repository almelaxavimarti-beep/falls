package io.github.almelaxavimarti.falls.midi;

import java.nio.charset.StandardCharsets;

/**
 * Lee los bytes de un archivo MIDI de uno en uno, llevando la cuenta de por dónde va.
 * Todos los números de MIDI están en big-endian: el byte más importante va primero.
 */
public final class LectorBytes {

    private final byte[] datos;
    private int posicion;

    public LectorBytes(byte[] datos) {
        this.datos = datos.clone();
        this.posicion = 0;
    }

    /** Byte por el que va (empezando en 0). */
    public int posicion() {
        return posicion;
    }

    /** Cuántos bytes quedan por leer. */
    public int restantes() {
        return datos.length - posicion;
    }

    /** Lee 1 byte sin signo (0-255). */
    public int leerUint8() throws ArchivoMidiCorruptoException {
        if (restantes() < 1) {
            throw new ArchivoMidiCorruptoException("El archivo se acaba antes de tiempo", posicion);
        }
        int valor = datos[posicion] & 0xFF;
        posicion++;
        return valor;
    }

    /** Lee 2 bytes en big-endian sin signo (0-65535). */
    public int leerUint16() throws ArchivoMidiCorruptoException {
        int alto = leerUint8();
        int bajo = leerUint8();
        return (alto << 8) | bajo;
    }

    /** Lee 4 bytes en big-endian sin signo (0-4.294.967.295). */
    public long leerUint32() throws ArchivoMidiCorruptoException {
        long valor = 0;
        for (int i = 0; i < 4; i++) {
            valor = (valor << 8) | leerUint8();
        }
        return valor;
    }

    /** Lee un texto ASCII de la longitud indicada (por ejemplo "MThd"). */
    public String leerTexto(int longitud) throws ArchivoMidiCorruptoException {
        if (restantes() < longitud) {
            throw new ArchivoMidiCorruptoException("El archivo se acaba antes de tiempo", posicion);
        }
        String texto = new String(datos, posicion, longitud, StandardCharsets.US_ASCII);
        posicion += longitud;
        return texto;
    }

    /**
     * Lee una "cantidad de longitud variable" (VLQ): 7 bits de dato por byte; si el bit más alto
     * del byte está a 1, sigue otro byte. Como mucho 4 bytes (valor máximo 0x0FFFFFFF).
     */
    public int leerCantidadVariable() throws ArchivoMidiCorruptoException {
        int inicio = posicion;
        int valor = 0;
        for (int i = 0; i < 4; i++) {
            int b = leerUint8();
            valor = (valor << 7) | (b & 0x7F);
            if ((b & 0x80) == 0) {
                return valor;
            }
        }
        throw new ArchivoMidiCorruptoException("Cantidad variable de más de 4 bytes", inicio);
    }

    /** Se salta los bytes indicados. */
    public void saltar(int cantidad) throws ArchivoMidiCorruptoException {
        if (restantes() < cantidad) {
            throw new ArchivoMidiCorruptoException("El archivo se acaba antes de tiempo", posicion);
        }
        posicion += cantidad;
    }
    /** Lee los bytes indicados tal cual (por ejemplo, los datos de un metaevento). */
    public byte[] leerBytes(int cantidad) throws ArchivoMidiCorruptoException {
        if (restantes() < cantidad) {
            throw new ArchivoMidiCorruptoException("El archivo se acaba antes de tiempo", posicion);
        }
        byte[] resultado = java.util.Arrays.copyOfRange(datos, posicion, posicion + cantidad);
        posicion += cantidad;
        return resultado;
    }
}
