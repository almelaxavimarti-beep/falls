package io.github.almelaxavimarti.falls.midi;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Escribe bytes en el formato de los archivos MIDI. Es lo contrario de {@link LectorBytes}:
 * números en big-endian, textos ASCII y cantidades de longitud variable.
 */
public final class EscritorBytes {

    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();

    /** Escribe 1 byte sin signo (0-255). */
    public void escribirUint8(int valor) {
        if (valor < 0 || valor > 0xFF) {
            throw new IllegalArgumentException("No cabe en 1 byte: " + valor);
        }
        salida.write(valor);
    }

    /** Escribe 2 bytes en big-endian (0-65535): primero el alto, luego el bajo. */
    public void escribirUint16(int valor) {
        if (valor < 0 || valor > 0xFFFF) {
            throw new IllegalArgumentException("No cabe en 2 bytes: " + valor);
        }
        salida.write(valor >> 8);
        salida.write(valor & 0xFF);
    }

    /** Escribe 4 bytes en big-endian (0-4.294.967.295). */
    public void escribirUint32(long valor) {
        if (valor < 0 || valor > 0xFFFF_FFFFL) {
            throw new IllegalArgumentException("No cabe en 4 bytes: " + valor);
        }
        for (int desplazamiento = 24; desplazamiento >= 0; desplazamiento -= 8) {
            salida.write((int) (valor >> desplazamiento) & 0xFF);
        }
    }

    /** Escribe un texto ASCII tal cual (por ejemplo "MThd"). */
    public void escribirTexto(String texto) {
        salida.writeBytes(texto.getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * Escribe una cantidad de longitud variable (VLQ): grupos de 7 bits, del más alto al más
     * bajo; todos los bytes llevan el bit alto a 1 menos el último. Como mucho 0x0FFFFFFF.
     */
    public void escribirCantidadVariable(int valor) {
        if (valor < 0 || valor > 0x0FFF_FFFF) {
            throw new IllegalArgumentException("Cantidad variable fuera de rango: " + valor);
        }
        int desplazamiento = 21;
        while (desplazamiento > 0 && (valor >> desplazamiento) == 0) {
            desplazamiento -= 7;
        }
        for (; desplazamiento > 0; desplazamiento -= 7) {
            salida.write(((valor >> desplazamiento) & 0x7F) | 0x80);
        }
        salida.write(valor & 0x7F);
    }

    /** Escribe unos bytes tal cual (por ejemplo, el cuerpo de una pista ya montada). */
    public void escribirBytes(byte[] bytes) {
        salida.writeBytes(bytes);
    }

    /** Cuántos bytes lleva escritos. */
    public int tamano() {
        return salida.size();
    }

    /** Devuelve una copia de todo lo escrito. */
    public byte[] aBytes() {
        return salida.toByteArray();
    }
}