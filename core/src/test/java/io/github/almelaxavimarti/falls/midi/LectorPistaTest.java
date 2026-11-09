package io.github.almelaxavimarti.falls.midi;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Lectura de los eventos de una pista MIDI ("MTrk"). */
class LectorPistaTest {

    // --- Ayudas para escribir pistas a mano ---

    /** Bytes sueltos (0-255) a un LectorBytes. */
    private static LectorBytes bytes(int... valores) {
        byte[] datos = new byte[valores.length];
        for (int i = 0; i < valores.length; i++) {
            datos[i] = (byte) valores[i];
        }
        return new LectorBytes(datos);
    }

    /** Una pista completa: "MTrk" + longitud + el cuerpo indicado. */
    private static int[] pista(int... cuerpo) {
        int n = cuerpo.length;
        int[] r = new int[8 + n];
        r[0] = 'M'; r[1] = 'T'; r[2] = 'r'; r[3] = 'k';
        r[4] = (n >>> 24) & 0xFF; r[5] = (n >>> 16) & 0xFF; r[6] = (n >>> 8) & 0xFF; r[7] = n & 0xFF;
        System.arraycopy(cuerpo, 0, r, 8, n);
        return r;
    }

    private static List<EventoMidi> leer(int... archivo) {
        try {
            return LectorPista.leer(bytes(archivo));
        } catch (ArchivoMidiCorruptoException e) {
            throw new AssertionError("No debería fallar: " + e.getMessage(), e);
        }
    }

    /** Solo los eventos de canal (sin metaeventos). */
    private static List<EventoCanal> deCanal(List<EventoMidi> eventos) {
        List<EventoCanal> r = new ArrayList<>();
        for (EventoMidi e : eventos) {
            if (e instanceof EventoCanal c) {
                r.add(c);
            }
        }
        return r;
    }

    private static final int[] FIN = {0x00, 0xFF, 0x2F, 0x00};

    private static int[] concat(int[]... partes) {
        int n = 0;
        for (int[] p : partes) n += p.length;
        int[] r = new int[n];
        int i = 0;
        for (int[] p : partes) {
            System.arraycopy(p, 0, r, i, p.length);
            i += p.length;
        }
        return r;
    }

    // --- Notas y tiempo ---

    @Test
    void leeUnaNotaQueEmpiezaYAcaba() {
        // esperar 0, Do4 (60) empieza con velocidad 96; esperar 480 (VLQ 83 60), Do4 acaba
        List<EventoCanal> eventos = deCanal(leer(pista(concat(
                new int[] {0x00, 0x90, 0x3C, 0x60, 0x83, 0x60, 0x80, 0x3C, 0x40}, FIN))));

        assertEquals(List.of(
                new EventoCanal(0, 0x90, 0, 60, 96),
                new EventoCanal(480, 0x80, 0, 60, 64)), eventos);
    }

    @Test
    void elTickSeAcumulaDesdeElPrincipioDeLaPista() {
        // Cada evento guarda cuánto esperar desde el anterior; el lector lo convierte en tick absoluto.
        List<EventoCanal> eventos = deCanal(leer(pista(concat(new int[] {
                0x00, 0x90, 0x3C, 0x60,
                0x81, 0x70, 0x80, 0x3C, 0x00,   // +240
                0x00, 0x90, 0x40, 0x60,         // +0
                0x85, 0x50, 0x80, 0x40, 0x00},  // +720
                FIN))));

        assertEquals(0, eventos.get(0).tick());
        assertEquals(240, eventos.get(1).tick());
        assertEquals(240, eventos.get(2).tick());
        assertEquals(960, eventos.get(3).tick());
    }

    @Test
    void separaElTipoYElCanal() {
        // 0x99 = nota empieza (0x90) en el canal 9 (la batería en General MIDI, el "canal 10")
        EventoCanal bombo = deCanal(leer(pista(concat(new int[] {0x00, 0x99, 0x24, 0x64}, FIN)))).get(0);

        assertEquals(EventoCanal.NOTA_EMPIEZA, bombo.tipo());
        assertEquals(9, bombo.canal());
        assertEquals(36, bombo.dato1());
        assertEquals(100, bombo.dato2());
    }

    @Test
    void entiendeElRunningStatus() {
        // Si un evento tiene el mismo estado que el anterior, el archivo puede omitirlo.
        // Aquí solo el primero lleva 0x90; los otros dos empiezan directamente por la nota.
        List<EventoCanal> eventos = deCanal(leer(pista(concat(new int[] {
                0x00, 0x90, 0x3C, 0x60,
                0x60, 0x40, 0x60,        // +96, sin estado: sigue siendo 0x90
                0x60, 0x3C, 0x00},       // +96, Do4 con velocidad 0
                FIN))));

        assertEquals(List.of(
                new EventoCanal(0, 0x90, 0, 60, 96),
                new EventoCanal(96, 0x90, 0, 64, 96),
                new EventoCanal(192, 0x90, 0, 60, 0)), eventos);
    }

    @Test
    void leeLosEventosDeUnoYDosDatos() {
        List<EventoCanal> eventos = deCanal(leer(pista(concat(new int[] {
                0x00, 0xB0, 0x40, 0x7F,   // controlador 64 (sustain) = 127: dos datos
                0x00, 0xC0, 0x05,         // instrumento 5: un dato
                0x00, 0xD0, 0x40,         // presión de canal: un dato
                0x00, 0xE0, 0x00, 0x40,   // pitch bend: dos datos
                0x00, 0xA0, 0x3C, 0x20},  // presión de tecla: dos datos
                FIN))));

        assertEquals(List.of(
                new EventoCanal(0, 0xB0, 0, 64, 127),
                new EventoCanal(0, 0xC0, 0, 5, 0),
                new EventoCanal(0, 0xD0, 0, 64, 0),
                new EventoCanal(0, 0xE0, 0, 0, 64),
                new EventoCanal(0, 0xA0, 0, 60, 32)), eventos);
    }

    // --- Metaeventos ---

    @Test
    void leeLosMetaeventos() {
        List<EventoMidi> eventos = leer(pista(concat(new int[] {
                0x00, 0xFF, 0x03, 0x04, 'B', 'a', 'j', 'o',      // nombre de pista
                0x00, 0xFF, 0x51, 0x03, 0x07, 0xA1, 0x20},       // tempo: 500.000 µs = 120 bpm
                FIN)));

        EventoMeta nombre = (EventoMeta) eventos.get(0);
        assertEquals(EventoMeta.NOMBRE_PISTA, nombre.tipo());
        assertArrayEquals(new byte[] {'B', 'a', 'j', 'o'}, nombre.datos());

        EventoMeta tempo = (EventoMeta) eventos.get(1);
        assertEquals(EventoMeta.TEMPO, tempo.tipo());
        assertArrayEquals(new byte[] {0x07, (byte) 0xA1, 0x20}, tempo.datos());
    }

    @Test
    void elFinDePistaEsElUltimoEvento() {
        List<EventoMidi> eventos = leer(pista(concat(new int[] {0x00, 0x90, 0x3C, 0x60}, FIN)));

        EventoMeta fin = (EventoMeta) eventos.get(eventos.size() - 1);
        assertEquals(EventoMeta.FIN_DE_PISTA, fin.tipo());
        assertEquals(2, eventos.size());
    }

    @Test
    void elMetaeventoNoRompeElRunningStatus() {
        // Según la especificación, los metaeventos no cambian el running status.
        List<EventoCanal> eventos = deCanal(leer(pista(concat(new int[] {
                0x00, 0x90, 0x3C, 0x60,
                0x00, 0xFF, 0x51, 0x03, 0x07, 0xA1, 0x20,
                0x00, 0x40, 0x60},
                FIN))));

        assertEquals(new EventoCanal(0, 0x90, 0, 64, 96), eventos.get(1));
    }

    // --- Lo que Falls se salta ---

    @Test
    void seSaltaLosMensajesSysEx() {
        // SysEx: mensajes propios de cada fabricante de sintetizadores. Falls no los usa.
        List<EventoMidi> eventos = leer(pista(concat(new int[] {
                0x00, 0xF0, 0x03, 0x7E, 0x7F, 0xF7,
                0x00, 0x90, 0x3C, 0x60},
                FIN)));

        assertEquals(List.of(new EventoCanal(0, 0x90, 0, 60, 96)), deCanal(eventos));
        assertEquals(2, eventos.size());
    }

    @Test
    void seSaltaLosBytesQueSobranTrasElFinDePista() {
        LectorBytes lector = bytes(concat(pista(concat(FIN, new int[] {0x00, 0x00})), new int[] {'M', 'T', 'r', 'k'}));

        try {
            LectorPista.leer(lector);
        } catch (ArchivoMidiCorruptoException e) {
            throw new AssertionError(e);
        }

        assertEquals(14, lector.posicion());
    }

    @Test
    void unaPistaSinFinDePistaSeLeeIgual() {
        // Algunos programas no escriben el fin de pista: Falls lo tolera.
        List<EventoMidi> eventos = leer(pista(0x00, 0x90, 0x3C, 0x60));

        assertEquals(List.of(new EventoCanal(0, 0x90, 0, 60, 96)), deCanal(eventos));
    }

    @Test
    void dejaElLectorJustoDespuesDeLaPista() {
        LectorBytes lector = bytes(concat(pista(FIN), pista(FIN)));

        try {
            LectorPista.leer(lector);
            assertEquals(12, lector.posicion());
            LectorPista.leer(lector);
            assertEquals(24, lector.posicion());
        } catch (ArchivoMidiCorruptoException e) {
            throw new AssertionError(e);
        }
    }

    // --- Archivos mal formados ---

    @Test
    void rechazaUnaPistaQueNoEmpiezaPorMTrk() {
        ArchivoMidiCorruptoException error = assertThrows(ArchivoMidiCorruptoException.class,
                () -> LectorPista.leer(bytes('M', 'T', 'h', 'd', 0, 0, 0, 0)));

        assertEquals(0, error.posicion());
        assertTrue(error.getMessage().contains("MTrk"));
    }

    @Test
    void rechazaUnaPistaMasLargaQueElArchivo() {
        assertThrows(ArchivoMidiCorruptoException.class,
                () -> LectorPista.leer(bytes('M', 'T', 'r', 'k', 0, 0, 0, 20, 0x00, 0x90, 0x3C, 0x60)));
    }

    @Test
    void rechazaElRunningStatusSinEstadoAnterior() {
        ArchivoMidiCorruptoException error = assertThrows(ArchivoMidiCorruptoException.class,
                () -> LectorPista.leer(bytes(pista(0x00, 0x3C, 0x60))));

        assertEquals(9, error.posicion());
    }

    @Test
    void rechazaUnByteDeDatosConElBitAlto() {
        // Los datos de un evento de canal van de 0 a 127: un 0x90 donde va la velocidad es un error.
        ArchivoMidiCorruptoException error = assertThrows(ArchivoMidiCorruptoException.class,
                () -> LectorPista.leer(bytes(pista(0x00, 0x90, 0x3C, 0x90))));

        assertEquals(11, error.posicion());
    }

    @Test
    void rechazaUnEventoQueSeSaleDeLaPista() {
        // La pista dice medir 3 bytes, pero el evento de nota necesita 4.
        assertThrows(ArchivoMidiCorruptoException.class,
                () -> LectorPista.leer(bytes('M', 'T', 'r', 'k', 0, 0, 0, 3, 0x00, 0x90, 0x3C, 0x60)));
    }

    @Test
    void rechazaUnEstadoDeSistemaNoValido() {
        // 0xF1-0xF6 y 0xF8-0xFE son mensajes de tiempo real que no pueden ir en un archivo.
        assertThrows(ArchivoMidiCorruptoException.class,
                () -> LectorPista.leer(bytes(pista(0x00, 0xF8))));
    }
}
