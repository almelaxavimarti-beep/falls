package io.github.almelaxavimarti.falls.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Lectura de bytes en el formato de los archivos MIDI. */
class LectorBytesTest {

    /** Crea un array de bytes a partir de números 0-255 (más cómodo de escribir que (byte) 0xFF). */
    private static LectorBytes lector(int... valores) {
        byte[] datos = new byte[valores.length];
        for (int i = 0; i < valores.length; i++) {
            datos[i] = (byte) valores[i];
        }
        return new LectorBytes(datos);
    }

    @Test
    void leeBytesSinSigno() {
        // En Java un byte va de -128 a 127, pero en MIDI cada byte vale de 0 a 255.
        LectorBytes lector = lector(0x00, 0x7F, 0x80, 0xFF);

        assertEquals(0, sinError(lector::leerUint8));
        assertEquals(127, sinError(lector::leerUint8));
        assertEquals(128, sinError(lector::leerUint8));
        assertEquals(255, sinError(lector::leerUint8));
    }

    @Test
    void leeDosBytesEnBigEndian() {
        LectorBytes lector = lector(0x03, 0xC0, 0x00, 0x60, 0xFF, 0xFF);

        assertEquals(960, sinError(lector::leerUint16));    // 3 * 256 + 192
        assertEquals(96, sinError(lector::leerUint16));
        assertEquals(65535, sinError(lector::leerUint16));
    }

    @Test
    void leeCuatroBytesEnBigEndian() {
        LectorBytes lector = lector(0x00, 0x00, 0x00, 0x06, 0x00, 0x07, 0xA1, 0x20, 0xFF, 0xFF, 0xFF, 0xFF);

        assertEquals(6L, sinError(lector::leerUint32));
        assertEquals(500_000L, sinError(lector::leerUint32));     // tempo de 120 bpm
        assertEquals(4_294_967_295L, sinError(lector::leerUint32)); // el máximo: no cabe en un int
    }

    @Test
    void leeTextoAscii() {
        LectorBytes lector = lector('M', 'T', 'h', 'd', 'M', 'T', 'r', 'k');

        assertEquals("MThd", sinError(() -> lector.leerTexto(4)));
        assertEquals("MTrk", sinError(() -> lector.leerTexto(4)));
    }

    @Test
    void leeCantidadesDeLongitudVariable() {
        // Ejemplos de la especificación Standard MIDI File 1.0.
        assertEquals(0, sinError(lector(0x00)::leerCantidadVariable));
        assertEquals(127, sinError(lector(0x7F)::leerCantidadVariable));
        assertEquals(128, sinError(lector(0x81, 0x00)::leerCantidadVariable));
        assertEquals(8192, sinError(lector(0xC0, 0x00)::leerCantidadVariable));
        assertEquals(16383, sinError(lector(0xFF, 0x7F)::leerCantidadVariable));
        assertEquals(16384, sinError(lector(0x81, 0x80, 0x00)::leerCantidadVariable));
        assertEquals(0x0FFFFFFF, sinError(lector(0xFF, 0xFF, 0xFF, 0x7F)::leerCantidadVariable));
    }

    @Test
    void laCantidadVariableAvanzaSoloLoQueOcupa() {
        LectorBytes lector = lector(0x81, 0x00, 0x7F);

        assertEquals(128, sinError(lector::leerCantidadVariable));
        assertEquals(2, lector.posicion());
        assertEquals(127, sinError(lector::leerCantidadVariable));
    }

    @Test
    void unaCantidadVariableDeMasDeCuatroBytesEsUnError() {
        LectorBytes lector = lector(0xFF, 0xFF, 0xFF, 0xFF, 0x7F);

        assertThrows(ArchivoMidiCorruptoException.class, lector::leerCantidadVariable);
    }

    @Test
    void llevaLaCuentaDeLaPosicionYDeLoQueQueda() {
        LectorBytes lector = lector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        assertEquals(0, lector.posicion());
        assertEquals(10, lector.restantes());

        sinError(lector::leerUint8);
        sinError(lector::leerUint16);
        assertEquals(3, lector.posicion());

        sinError(() -> {
            lector.saltar(4);
            return null;
        });
        assertEquals(7, lector.posicion());
        assertEquals(3, lector.restantes());
    }

    @Test
    void leerMasAllaDelFinalDaUnErrorConLaPosicion() {
        LectorBytes lector = lector(0x01, 0x02, 0x03);
        sinError(lector::leerUint16);
        sinError(lector::leerUint8);

        ArchivoMidiCorruptoException error =
                assertThrows(ArchivoMidiCorruptoException.class, lector::leerUint8);

        assertEquals(3, error.posicion());
        assertTrue(error.getMessage().contains("byte 3"));
    }

    @Test
    void todasLasLecturasDetectanElFinalDelArchivo() {
        assertThrows(ArchivoMidiCorruptoException.class, lector()::leerUint8);
        assertThrows(ArchivoMidiCorruptoException.class, lector(0, 0, 0)::leerUint32);
        assertThrows(ArchivoMidiCorruptoException.class, () -> lector('M', 'T').leerTexto(4));
        assertThrows(ArchivoMidiCorruptoException.class, lector(0x81)::leerCantidadVariable);
        assertThrows(ArchivoMidiCorruptoException.class, () -> lector(1, 2).saltar(3));
    }

    @Test
    void noCambiaSiCambiaElArrayOriginal() {
        byte[] datos = {10, 20};
        LectorBytes lector = new LectorBytes(datos);

        datos[0] = 99;

        assertEquals(10, sinError(lector::leerUint8));
    }

    // --- Ayuda para los tests: ejecuta una lectura que no debería fallar ---

    private interface Lectura<T> {
        T leer() throws ArchivoMidiCorruptoException;
    }

    private static <T> T sinError(Lectura<T> lectura) {
        try {
            return lectura.leer();
        } catch (ArchivoMidiCorruptoException e) {
            throw new AssertionError("No debería fallar: " + e.getMessage(), e);
        }
    }
}
