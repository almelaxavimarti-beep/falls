package io.github.almelaxavimarti.falls.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** La cabecera "MThd" de un archivo MIDI. */
class CabeceraMidiTest {

    private static LectorBytes lector(int... valores) {
        byte[] datos = new byte[valores.length];
        for (int i = 0; i < valores.length; i++) {
            datos[i] = (byte) valores[i];
        }
        return new LectorBytes(datos);
    }

    private static CabeceraMidi leer(LectorBytes lector) {
        try {
            return CabeceraMidi.leer(lector);
        } catch (ArchivoMidiCorruptoException e) {
            throw new AssertionError("No debería fallar: " + e.getMessage(), e);
        }
    }

    @Test
    void leeUnaCabeceraDeFormato1() {
        // "MThd", longitud 6, formato 1, 3 pistas, 960 ticks por negra
        CabeceraMidi cabecera = leer(lector(
                'M', 'T', 'h', 'd', 0, 0, 0, 6, 0, 1, 0, 3, 0x03, 0xC0));

        assertEquals(1, cabecera.formato());
        assertEquals(3, cabecera.numeroPistas());
        assertEquals(960, cabecera.ticksPorNegra());
    }

    @Test
    void leeUnaCabeceraDeFormato0Con96TicksComoFlStudio() {
        CabeceraMidi cabecera = leer(lector(
                'M', 'T', 'h', 'd', 0, 0, 0, 6, 0, 0, 0, 1, 0x00, 0x60));

        assertEquals(0, cabecera.formato());
        assertEquals(1, cabecera.numeroPistas());
        assertEquals(96, cabecera.ticksPorNegra());
    }

    @Test
    void dejaElLectorJustoDespuesDeLaCabecera() {
        LectorBytes lector = lector('M', 'T', 'h', 'd', 0, 0, 0, 6, 0, 1, 0, 2, 0x01, 0xE0, 'M', 'T', 'r', 'k');

        leer(lector);

        assertEquals(14, lector.posicion());
    }

    @Test
    void seSaltaLosBytesExtraSiLaCabeceraEsMasLarga() {
        // La especificación permite que versiones futuras añadan datos: hay que saltarlos.
        LectorBytes lector = lector('M', 'T', 'h', 'd', 0, 0, 0, 8, 0, 1, 0, 2, 0x01, 0xE0, 0xAA, 0xBB);

        CabeceraMidi cabecera = leer(lector);

        assertEquals(480, cabecera.ticksPorNegra());
        assertEquals(16, lector.posicion());
    }

    @Test
    void rechazaUnArchivoQueNoEmpiezaPorMThd() {
        // Así empieza, por ejemplo, un archivo WAV.
        ArchivoMidiCorruptoException error = assertThrows(ArchivoMidiCorruptoException.class,
                () -> CabeceraMidi.leer(lector('R', 'I', 'F', 'F', 0, 0, 0, 6, 0, 1, 0, 1, 0, 96)));

        assertEquals(0, error.posicion());
        assertTrue(error.getMessage().contains("MIDI"));
    }

    @Test
    void rechazaUnaCabeceraDemasiadoCorta() {
        assertThrows(ArchivoMidiCorruptoException.class,
                () -> CabeceraMidi.leer(lector('M', 'T', 'h', 'd', 0, 0, 0, 4, 0, 1, 0, 1)));
    }

    @Test
    void rechazaElFormato2() {
        // Formato 2 = varias secuencias independientes; casi no se usa y Falls no lo soporta.
        ArchivoMidiCorruptoException error = assertThrows(ArchivoMidiCorruptoException.class,
                () -> CabeceraMidi.leer(lector('M', 'T', 'h', 'd', 0, 0, 0, 6, 0, 2, 0, 1, 0, 96)));

        assertTrue(error.getMessage().contains("formato 2"));
    }

    @Test
    void rechazaUnArchivoSinPistas() {
        assertThrows(ArchivoMidiCorruptoException.class,
                () -> CabeceraMidi.leer(lector('M', 'T', 'h', 'd', 0, 0, 0, 6, 0, 1, 0, 0, 0, 96)));
    }

    @Test
    void rechazaElTiempoSmpte() {
        // Si el bit más alto de la división está a 1, el tiempo va en fotogramas (SMPTE), no en negras.
        ArchivoMidiCorruptoException error = assertThrows(ArchivoMidiCorruptoException.class,
                () -> CabeceraMidi.leer(lector('M', 'T', 'h', 'd', 0, 0, 0, 6, 0, 1, 0, 1, 0xE7, 0x28)));

        assertTrue(error.getMessage().contains("SMPTE"));
    }

    @Test
    void rechazaCeroTicksPorNegra() {
        assertThrows(ArchivoMidiCorruptoException.class,
                () -> CabeceraMidi.leer(lector('M', 'T', 'h', 'd', 0, 0, 0, 6, 0, 1, 0, 1, 0, 0)));
    }

    @Test
    void rechazaUnArchivoCortado() {
        assertThrows(ArchivoMidiCorruptoException.class,
                () -> CabeceraMidi.leer(lector('M', 'T', 'h', 'd', 0, 0, 0, 6, 0, 1)));
    }
}
