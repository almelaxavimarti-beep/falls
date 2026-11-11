package io.github.almelaxavimarti.falls.midi;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Escribir bytes en formato MIDI: lo contrario de LectorBytes. */
class EscritorBytesTest {

    private final EscritorBytes escritor = new EscritorBytes();

    private static byte[] bytes(int... valores) {
        byte[] r = new byte[valores.length];
        for (int i = 0; i < valores.length; i++) {
            r[i] = (byte) valores[i];
        }
        return r;
    }

    @Test
    void empiezaVacio() {
        assertEquals(0, escritor.tamano());
        assertArrayEquals(new byte[0], escritor.aBytes());
    }

    @Test
    void escribeUnByte() {
        escritor.escribirUint8(0);
        escritor.escribirUint8(0x90);
        escritor.escribirUint8(255);

        assertArrayEquals(bytes(0x00, 0x90, 0xFF), escritor.aBytes());
    }

    @Test
    void escribeDosBytesEnBigEndian() {
        escritor.escribirUint16(960);   // 0x03C0

        assertArrayEquals(bytes(0x03, 0xC0), escritor.aBytes());
    }

    @Test
    void escribeCuatroBytesEnBigEndian() {
        escritor.escribirUint32(6);
        escritor.escribirUint32(0xFFFF_FFFFL);

        assertArrayEquals(bytes(0, 0, 0, 6, 0xFF, 0xFF, 0xFF, 0xFF), escritor.aBytes());
    }

    @Test
    void escribeTextoAscii() {
        escritor.escribirTexto("MTrk");

        assertArrayEquals(bytes('M', 'T', 'r', 'k'), escritor.aBytes());
    }

    @Test
    void escribeCantidadesVariablesComoEnLaEspecificacion() {
        // Ejemplos de la tabla de la especificación SMF 1.0
        comprobarCantidad(0x00, 0x00);
        comprobarCantidad(0x40, 0x40);
        comprobarCantidad(0x7F, 0x7F);
        comprobarCantidad(0x80, 0x81, 0x00);
        comprobarCantidad(0x2000, 0xC0, 0x00);
        comprobarCantidad(0x3FFF, 0xFF, 0x7F);
        comprobarCantidad(0x4000, 0x81, 0x80, 0x00);
        comprobarCantidad(0x1FFFFF, 0xFF, 0xFF, 0x7F);
        comprobarCantidad(0x200000, 0x81, 0x80, 0x80, 0x00);
        comprobarCantidad(0x0FFFFFFF, 0xFF, 0xFF, 0xFF, 0x7F);
    }

    private static void comprobarCantidad(int valor, int... esperado) {
        EscritorBytes e = new EscritorBytes();
        e.escribirCantidadVariable(valor);
        assertArrayEquals(bytes(esperado), e.aBytes(), "cantidad variable " + valor);
    }

    @Test
    void escribeBytesTalCual() {
        escritor.escribirUint8(1);
        escritor.escribirBytes(bytes(2, 3, 4));

        assertArrayEquals(bytes(1, 2, 3, 4), escritor.aBytes());
        assertEquals(4, escritor.tamano());
    }

    @Test
    void loQueEscribeLoLeeElLector() throws ArchivoMidiCorruptoException {
        escritor.escribirTexto("MThd");
        escritor.escribirUint32(6);
        escritor.escribirUint16(1);
        escritor.escribirUint8(0x90);
        escritor.escribirCantidadVariable(7680);

        LectorBytes lector = new LectorBytes(escritor.aBytes());
        assertEquals("MThd", lector.leerTexto(4));
        assertEquals(6, lector.leerUint32());
        assertEquals(1, lector.leerUint16());
        assertEquals(0x90, lector.leerUint8());
        assertEquals(7680, lector.leerCantidadVariable());
        assertEquals(0, lector.restantes());
    }

    @Test
    void aBytesDevuelveUnaCopia() {
        escritor.escribirUint8(1);
        byte[] copia = escritor.aBytes();
        copia[0] = 99;

        assertArrayEquals(bytes(1), escritor.aBytes());
    }

    @Test
    void rechazaValoresQueNoCaben() {
        assertThrows(IllegalArgumentException.class, () -> escritor.escribirUint8(-1));
        assertThrows(IllegalArgumentException.class, () -> escritor.escribirUint8(256));
        assertThrows(IllegalArgumentException.class, () -> escritor.escribirUint16(-1));
        assertThrows(IllegalArgumentException.class, () -> escritor.escribirUint16(65536));
        assertThrows(IllegalArgumentException.class, () -> escritor.escribirUint32(-1));
        assertThrows(IllegalArgumentException.class, () -> escritor.escribirUint32(0x1_0000_0000L));
        assertThrows(IllegalArgumentException.class, () -> escritor.escribirCantidadVariable(-1));
        assertThrows(IllegalArgumentException.class, () -> escritor.escribirCantidadVariable(0x1000_0000));
        assertEquals(0, escritor.tamano(), "si falla no escribe nada");
    }
}
