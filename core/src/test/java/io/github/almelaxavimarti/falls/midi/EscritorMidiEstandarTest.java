package io.github.almelaxavimarti.falls.midi;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.almelaxavimarti.falls.modelo.CambioCompas;
import io.github.almelaxavimarti.falls.modelo.CambioTempo;
import io.github.almelaxavimarti.falls.modelo.CambioTonalidad;
import io.github.almelaxavimarti.falls.modelo.Cancion;
import io.github.almelaxavimarti.falls.modelo.Controlador;
import io.github.almelaxavimarti.falls.modelo.Nota;
import io.github.almelaxavimarti.falls.modelo.PitchBend;
import io.github.almelaxavimarti.falls.modelo.Pista;
import io.github.almelaxavimarti.falls.modelo.Rol;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** El escritor MIDI: de una Cancion a los bytes de un archivo .mid, y vuelta. */
class EscritorMidiEstandarTest {

    private final EscritorMidi escritor = new EscritorMidiEstandar();
    private final LectorMidi lector = new LectorMidiEstandar();

    // --- Ayudas ---

    private static byte[] bytes(int... valores) {
        byte[] r = new byte[valores.length];
        for (int i = 0; i < valores.length; i++) {
            r[i] = (byte) valores[i];
        }
        return r;
    }

    private static Cancion cancion(List<CambioTempo> tempos, List<CambioCompas> compases,
                                   List<CambioTonalidad> tonalidades, List<Pista> pistas) {
        return new Cancion(tempos, compases, tonalidades, pistas);
    }

    private static Cancion soloPistas(Pista... pistas) {
        return cancion(List.of(), List.of(), List.of(), List.of(pistas));
    }

    /** Devuelve el cuerpo (sin "MTrk" ni la longitud) de la pista número n del archivo (0 = la de tempo). */
    private static byte[] cuerpoDePista(byte[] archivo, int n) {
        int posicion = 14;                                  // después de la cabecera MThd
        for (int i = 0; ; i++) {
            int longitud = ((archivo[posicion + 4] & 0xFF) << 24) | ((archivo[posicion + 5] & 0xFF) << 16)
                    | ((archivo[posicion + 6] & 0xFF) << 8) | (archivo[posicion + 7] & 0xFF);
            if (i == n) {
                return Arrays.copyOfRange(archivo, posicion + 8, posicion + 8 + longitud);
            }
            posicion += 8 + longitud;
        }
    }

    private Cancion releer(Cancion cancion) {
        try {
            return lector.leer(escritor.escribir(cancion));
        } catch (ArchivoMidiCorruptoException e) {
            throw new AssertionError("El lector no entiende lo que ha escrito el escritor: " + e.getMessage(), e);
        }
    }

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

    // --- Bytes exactos ---

    @Test
    void unaCancionVaciaEsUnaCabeceraYUnaPistaDeTempoVacia() {
        byte[] archivo = escritor.escribir(soloPistas());

        assertArrayEquals(bytes(
                'M', 'T', 'h', 'd', 0, 0, 0, 6,
                0, 1,                // formato 1
                0, 1,                // 1 pista (la de tempo)
                0x03, 0xC0,          // 960 ticks por negra
                'M', 'T', 'r', 'k', 0, 0, 0, 4,
                0x00, 0xFF, 0x2F, 0x00), archivo);
    }

    @Test
    void laCabeceraCuentaLaPistaDeTempoMasLasDeMusica() {
        Pista a = new Pista("A", Rol.DESCONOCIDO, List.of(new Nota(60, 0, 960, 100, 0)));
        Pista b = new Pista("B", Rol.DESCONOCIDO, List.of(new Nota(48, 0, 960, 100, 1)));

        byte[] archivo = escritor.escribir(soloPistas(a, b));

        assertEquals(3, archivo[11] & 0xFF, "número de pistas");
    }

    @Test
    void escribeTempoCompasYTonalidadEnLaPrimeraPista() {
        byte[] archivo = escritor.escribir(cancion(
                List.of(new CambioTempo(0, 120.0)),
                List.of(new CambioCompas(0, 3, 4)),
                List.of(new CambioTonalidad(0, -3, true)),     // Do menor: 3 bemoles
                List.of()));

        assertArrayEquals(bytes(
                0x00, 0xFF, 0x51, 0x03, 0x07, 0xA1, 0x20,      // 500.000 µs por negra
                0x00, 0xFF, 0x58, 0x04, 3, 2, 24, 8,           // 3/4 (4 = 2^2)
                0x00, 0xFF, 0x59, 0x02, 0xFD, 0x01,            // -3 con signo = 0xFD, menor
                0x00, 0xFF, 0x2F, 0x00), cuerpoDePista(archivo, 0));
    }

    @Test
    void escribeUnaNotaConSuNombreDePista() {
        Pista bajo = new Pista("Bajo", Rol.DESCONOCIDO, List.of(new Nota(36, 0, 960, 100, 2)));

        byte[] archivo = escritor.escribir(soloPistas(bajo));

        assertArrayEquals(bytes(
                0x00, 0xFF, 0x03, 0x04, 'B', 'a', 'j', 'o',
                0x00, 0x92, 36, 100,                           // empieza, canal 3
                0x87, 0x40, 0x82, 36, 64,                      // +960: acaba
                0x00, 0xFF, 0x2F, 0x00), cuerpoDePista(archivo, 1));
    }

    @Test
    void unaPistaSinNombreNoLlevaMetaeventoDeNombre() {
        Pista sinNombre = new Pista("", Rol.DESCONOCIDO, List.of(new Nota(60, 480, 480, 90, 0)));

        assertArrayEquals(bytes(
                0x83, 0x60, 0x90, 60, 90,                      // +480
                0x83, 0x60, 0x80, 60, 64,                      // +480
                0x00, 0xFF, 0x2F, 0x00), cuerpoDePista(escritor.escribir(soloPistas(sinNombre)), 1));
    }

    @Test
    void enElMismoTickPrimeroAcabaUnaNotaYLuegoEmpiezaLaSiguiente() {
        // Dos Do4 seguidos: si el segundo empezara antes de que acabe el primero, se cortaría.
        Pista pista = new Pista("", Rol.DESCONOCIDO, List.of(
                new Nota(60, 0, 960, 100, 0),
                new Nota(60, 960, 960, 100, 0)));

        assertArrayEquals(bytes(
                0x00, 0x90, 60, 100,
                0x87, 0x40, 0x80, 60, 64,
                0x00, 0x90, 60, 100,
                0x87, 0x40, 0x80, 60, 64,
                0x00, 0xFF, 0x2F, 0x00), cuerpoDePista(escritor.escribir(soloPistas(pista)), 1));
    }

    @Test
    void escribeControladoresYPitchBend() {
        Pista pista = new Pista("", Rol.DESCONOCIDO, List.of(),
                List.of(new Controlador(0, 0, Controlador.SUSTAIN, 127)),
                List.of(new PitchBend(0, 0, PitchBend.CENTRO), new PitchBend(10, 0, 16383)));

        assertArrayEquals(bytes(
                0x00, 0xB0, 64, 127,
                0x00, 0xE0, 0x00, 0x40,                        // 8192: bajo 0, alto 64
                0x0A, 0xE0, 0x7F, 0x7F,                        // 16383: todo a 1
                0x00, 0xFF, 0x2F, 0x00), cuerpoDePista(escritor.escribir(soloPistas(pista)), 1));
    }

    // --- Ida y vuelta: escribir y releer da la misma canción ---

    @Test
    void idaYVueltaDeUnaCancionCompleta() {
        Cancion original = cancion(
                List.of(new CambioTempo(0, 120.0), CambioTempo.desdeMicrosegundos(3840, 666_667)),
                List.of(new CambioCompas(0, 4, 4), new CambioCompas(7680, 6, 8)),
                List.of(new CambioTonalidad(0, -3, true), new CambioTonalidad(7680, 2, false)),
                List.of(
                        new Pista("Melodía", Rol.DESCONOCIDO,
                                List.of(new Nota(60, 0, 480, 80, 0),
                                        new Nota(64, 0, 960, 90, 0),        // acorde y notas solapadas
                                        new Nota(67, 240, 240, 100, 0),
                                        new Nota(72, 7680, 3840, 127, 0)),
                                List.of(new Controlador(0, 0, Controlador.SUSTAIN, 127),
                                        new Controlador(960, 0, Controlador.SUSTAIN, 0)),
                                List.of(new PitchBend(100, 0, 0), new PitchBend(200, 0, PitchBend.CENTRO))),
                        new Pista("Batería", Rol.BATERIA,
                                List.of(new Nota(36, 0, 120, 110, 9), new Nota(42, 0, 60, 70, 9),
                                        new Nota(38, 960, 120, 100, 9))),
                        new Pista("", Rol.DESCONOCIDO,
                                List.of(new Nota(40, 0, 1_000_000, 64, 15)))));

        assertEquals(original, releer(original));
    }

    @Test
    void idaYVueltaDeLosMidisDePrueba() {
        for (String nombre : List.of(
                "01_referencia_do_mayor.mid", "02_notas_fuera_de_escala.mid", "03_choque_con_acorde.mid",
                "04_intervalos_asperos.mid", "05_velocidades_planas.mid", "06_la_menor_96ppq_varios_errores.mid")) {
            Cancion leida = leerMidiDePrueba(nombre);
            assertEquals(leida, releer(leida), nombre);
        }
    }

    // --- Lo que no se puede guardar en MIDI ---

    @Test
    void rechazaTemposQueNoCabenEnMidi() {
        // MIDI guarda el tempo en 3 bytes de microsegundos por negra: menos de unos 3,6 bpm no cabe.
        assertThrows(IllegalArgumentException.class,
                () -> escritor.escribir(cancion(List.of(new CambioTempo(0, 1.0)), List.of(), List.of(), List.of())));
        assertThrows(IllegalArgumentException.class,
                () -> escritor.escribir(cancion(List.of(new CambioTempo(0, 1e9)), List.of(), List.of(), List.of())));
    }

    @Test
    void rechazaUnNumeradorQueNoCabeEnUnByte() {
        assertThrows(IllegalArgumentException.class,
                () -> escritor.escribir(cancion(List.of(), List.of(new CambioCompas(0, 300, 4)), List.of(), List.of())));
    }

    @Test
    void rechazaDemasiadoTiempoEntreDosEventos() {
        Pista lejana = new Pista("", Rol.DESCONOCIDO, List.of(new Nota(60, 0x1000_0000L, 1, 100, 0)));

        assertThrows(IllegalArgumentException.class, () -> escritor.escribir(soloPistas(lejana)));
    }

    @Test
    void rechazaUnaCancionNull() {
        assertThrows(NullPointerException.class, () -> escritor.escribir(null));
    }
}
