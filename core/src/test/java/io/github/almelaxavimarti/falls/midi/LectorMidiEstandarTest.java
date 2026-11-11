package io.github.almelaxavimarti.falls.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.almelaxavimarti.falls.modelo.CambioCompas;
import io.github.almelaxavimarti.falls.modelo.CambioTempo;
import io.github.almelaxavimarti.falls.modelo.Cancion;
import io.github.almelaxavimarti.falls.modelo.Nota;
import io.github.almelaxavimarti.falls.modelo.Pista;
import io.github.almelaxavimarti.falls.modelo.Rol;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;

/** El lector MIDI completo: cabecera + pistas + conversión al modelo. */
class LectorMidiEstandarTest {

    private final LectorMidi lector = new LectorMidiEstandar();

    // --- Ayudas para construir archivos a mano ---

    private static byte[] archivo(int... valores) {
        byte[] datos = new byte[valores.length];
        for (int i = 0; i < valores.length; i++) {
            datos[i] = (byte) valores[i];
        }
        return datos;
    }

    private static int[] cabecera(int formato, int pistas, int ticksPorNegra) {
        return new int[] {'M', 'T', 'h', 'd', 0, 0, 0, 6, 0, formato, 0, pistas,
                (ticksPorNegra >> 8) & 0xFF, ticksPorNegra & 0xFF};
    }

    private static int[] bloque(char a, char b, char c, char d, int... cuerpo) {
        int n = cuerpo.length;
        int[] r = new int[8 + n];
        r[0] = a; r[1] = b; r[2] = c; r[3] = d;
        r[4] = (n >>> 24) & 0xFF; r[5] = (n >>> 16) & 0xFF; r[6] = (n >>> 8) & 0xFF; r[7] = n & 0xFF;
        System.arraycopy(cuerpo, 0, r, 8, n);
        return r;
    }

    private static int[] pista(int... cuerpo) {
        return bloque('M', 'T', 'r', 'k', cuerpo);
    }

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

    private Cancion leer(int[] bytes) {
        try {
            return lector.leer(archivo(bytes));
        } catch (ArchivoMidiCorruptoException e) {
            throw new AssertionError("No debería fallar: " + e.getMessage(), e);
        }
    }

    /** Lee uno de los MIDIs de prueba de src/test/resources/midi. */
    private Cancion leerMidiDePrueba(String nombre) {
        try (InputStream entrada = getClass().getResourceAsStream("/midi/" + nombre)) {
            if (entrada == null) {
                throw new AssertionError("No se encuentra el MIDI de prueba " + nombre);
            }
            return lector.leer(entrada.readAllBytes());
        } catch (IOException e) {
            throw new AssertionError("No debería fallar: " + e.getMessage(), e);
        }
    }

    // --- Archivos hechos a mano ---

    @Test
    void leeUnFormato1ConPistaDeTempoYUnaMelodia() {
        Cancion cancion = leer(concat(
                cabecera(1, 2, 96),
                pista(0x00, 0xFF, 0x51, 0x03, 0x07, 0xA1, 0x20,       // tempo 120
                      0x00, 0xFF, 0x58, 0x04, 4, 2, 24, 8,             // compás 4/4
                      0x00, 0xFF, 0x2F, 0x00),
                pista(0x00, 0xFF, 0x03, 0x07, 'M', 'e', 'l', 'o', 'd', 'i', 'a',
                      0x00, 0x90, 0x3C, 0x60,
                      0x60, 0x80, 0x3C, 0x00,                          // +96 = una negra
                      0x00, 0xFF, 0x2F, 0x00)));

        assertEquals(List.of(new CambioTempo(0, 120.0)), cancion.tempos());
        assertEquals(List.of(new CambioCompas(0, 4, 4)), cancion.compases());
        assertEquals(1, cancion.pistas().size(), "la pista de tempo no tiene música: no se incluye");

        Pista melodia = cancion.pistas().get(0);
        assertEquals("Melodia", melodia.nombre());
        assertEquals(List.of(new Nota(60, 0, 960, 96, 0)), melodia.notas());
    }

    @Test
    void juntaLosCambiosDeTodasLasPistasOrdenadosPorTick() {
        Cancion cancion = leer(concat(
                cabecera(1, 2, 960),
                pista(0x87, 0x40, 0xFF, 0x51, 0x03, 0x0A, 0x2C, 0x2B,   // +960: 90 bpm
                      0x00, 0xFF, 0x2F, 0x00),
                pista(0x00, 0xFF, 0x51, 0x03, 0x07, 0xA1, 0x20,          // 0: 120 bpm
                      0x00, 0x90, 0x3C, 0x60, 0x87, 0x40, 0x80, 0x3C, 0x00,
                      0x00, 0xFF, 0x2F, 0x00)));

        assertEquals(List.of(0L, 960L), cancion.tempos().stream().map(CambioTempo::tick).toList());
    }

    @Test
    void elFormato0SeSeparaEnUnaPistaPorCanal() {
        Cancion cancion = leer(concat(
                cabecera(0, 1, 96),
                pista(0x00, 0xFF, 0x51, 0x03, 0x07, 0xA1, 0x20,
                      0x00, 0x90, 0x3C, 0x60,          // canal 0: Do4
                      0x00, 0x99, 0x24, 0x64,          // canal 9: bombo
                      0x60, 0x80, 0x3C, 0x00,
                      0x00, 0x89, 0x24, 0x00,
                      0x00, 0xFF, 0x2F, 0x00)));

        assertEquals(List.of(new CambioTempo(0, 120.0)), cancion.tempos());
        assertEquals(2, cancion.pistas().size());

        Pista canal1 = cancion.pistas().get(0);
        Pista canal10 = cancion.pistas().get(1);
        assertEquals("Canal 1", canal1.nombre());
        assertEquals(List.of(new Nota(60, 0, 960, 96, 0)), canal1.notas());
        assertEquals("Canal 10", canal10.nombre());
        assertEquals(Rol.BATERIA, canal10.rol());
    }

    @Test
    void unFormato0ConUnSoloCanalConservaSuNombre() {
        Cancion cancion = leer(concat(
                cabecera(0, 1, 96),
                pista(0x00, 0xFF, 0x03, 0x04, 'B', 'a', 'j', 'o',
                      0x00, 0x90, 0x24, 0x60, 0x60, 0x80, 0x24, 0x00,
                      0x00, 0xFF, 0x2F, 0x00)));

        assertEquals(1, cancion.pistas().size());
        assertEquals("Bajo", cancion.pistas().get(0).nombre());
    }

    @Test
    void seSaltaLosBloquesDesconocidos() {
        // La especificación dice que un lector debe ignorar los bloques que no conoce.
        Cancion cancion = leer(concat(
                cabecera(1, 1, 96),
                bloque('X', 'F', 'I', 'H', 0xAA, 0xBB, 0xCC),
                pista(0x00, 0x90, 0x3C, 0x60, 0x60, 0x80, 0x3C, 0x00, 0x00, 0xFF, 0x2F, 0x00)));

        assertEquals(1, cancion.pistas().get(0).notas().size());
    }

    @Test
    void rechazaUnArchivoAlQueLeFaltanPistas() {
        ArchivoMidiCorruptoException error = assertThrows(ArchivoMidiCorruptoException.class,
                () -> lector.leer(archivo(concat(
                        cabecera(1, 2, 96),
                        pista(0x00, 0xFF, 0x2F, 0x00)))));

        assertTrue(error.getMessage().contains("Faltan pistas"));
    }

    @Test
    void rechazaUnBloqueDesconocidoMasLargoQueElArchivo() {
        assertThrows(ArchivoMidiCorruptoException.class,
                () -> lector.leer(archivo(concat(
                        cabecera(1, 1, 96),
                        new int[] {'X', 'F', 'I', 'H', 0, 0, 0, 50, 1, 2}))));
    }

    @Test
    void rechazaLoQueNoEsUnMidi() {
        assertThrows(ArchivoMidiCorruptoException.class,
                () -> lector.leer(archivo('R', 'I', 'F', 'F', 0, 0, 0, 0)));
    }

    // --- Los MIDIs de prueba (copias de C:\Falls\04_musica\midis-prueba) ---

    @Test
    void leeElMidiDeReferencia() {
        Cancion cancion = leerMidiDePrueba("01_referencia_do_mayor.mid");

        assertEquals(List.of(new CambioTempo(0, 120.0)), cancion.tempos());
        assertEquals(List.of(new CambioCompas(0, 4, 4)), cancion.compases());
        assertEquals(1, cancion.pistas().size());

        List<Nota> notas = cancion.pistas().get(0).notas();
        assertEquals(26, notas.size());
        // Primera nota: Mi4 en el tiempo 1, una negra (el archivo acaba la nota 1 tick antes, como muchos DAW)
        assertEquals(new Nota(64, 0, 959, 96, 0), notas.get(0));
        // Última nota: Do4 redonda en el compás 8
        assertEquals(new Nota(60, 7 * 4 * 960, 4 * 960 - 1, 88, 0), notas.get(25));
    }

    @Test
    void leeLasNotasFueraDeEscalaDondeDiceElLeeme() {
        List<Nota> notas = leerMidiDePrueba("02_notas_fuera_de_escala.mid").pistas().get(0).notas();

        // Compás 2, tiempo 1 = tick 3840: Mi bemol 4 (63)
        Nota mib = notas.stream().filter(n -> n.inicio() == 3840).findFirst().orElseThrow();
        assertEquals(63, mib.altura());
    }

    @Test
    void leeLaMelodiaYLosAcordesEnDosPistas() {
        Cancion cancion = leerMidiDePrueba("03_choque_con_acorde.mid");

        assertEquals(2, cancion.pistas().size());
        assertEquals("Melodia", cancion.pistas().get(0).nombre());
        assertEquals("Acordes", cancion.pistas().get(1).nombre());
        assertEquals(24, cancion.pistas().get(1).notas().size(), "8 acordes de 3 notas");
        assertTrue(cancion.pistas().get(1).notas().stream().allMatch(n -> n.canal() == 1));
        assertEquals(100.0, cancion.tempos().get(0).bpm(), 1e-9);
    }

    @Test
    void leeUnMidiA96TicksConCambioDeTempo() {
        // Como exporta FL Studio: 96 ticks por negra. El tempo pasa de 120 a 90 en el compás 5.
        Cancion cancion = leerMidiDePrueba("06_la_menor_96ppq_varios_errores.mid");

        assertEquals(List.of(
                new CambioTempo(0, 120.0),
                CambioTempo.desdeMicrosegundos(4 * 4 * 960, 666_667)), cancion.tempos());

        List<Nota> notas = cancion.pistas().get(0).notas();
        assertEquals(27, notas.size());
        // Las negras acaban 1 tick antes a 96 por negra = 10 ticks antes a 960
        assertEquals(new Nota(69, 0, 950, 92, 0), notas.get(0));
    }

    @Test
    void leeTodosLosMidisDePruebaSinErrores() {
        for (String nombre : List.of(
                "01_referencia_do_mayor.mid", "02_notas_fuera_de_escala.mid", "03_choque_con_acorde.mid",
                "04_intervalos_asperos.mid", "05_velocidades_planas.mid", "06_la_menor_96ppq_varios_errores.mid")) {
            Cancion cancion = leerMidiDePrueba(nombre);
            assertTrue(!cancion.pistas().isEmpty(), nombre + " debería tener pistas");
        }
    }
}
