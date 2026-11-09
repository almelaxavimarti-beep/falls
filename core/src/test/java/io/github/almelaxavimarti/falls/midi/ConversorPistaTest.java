package io.github.almelaxavimarti.falls.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.almelaxavimarti.falls.modelo.CambioCompas;
import io.github.almelaxavimarti.falls.modelo.CambioTempo;
import io.github.almelaxavimarti.falls.modelo.CambioTonalidad;
import io.github.almelaxavimarti.falls.modelo.Controlador;
import io.github.almelaxavimarti.falls.modelo.Nota;
import io.github.almelaxavimarti.falls.modelo.PitchBend;
import io.github.almelaxavimarti.falls.modelo.Rol;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Conversión de los eventos crudos de una pista en notas, controladores y cambios. */
class ConversorPistaTest {

    // --- Ayudas para escribir eventos ---

    private static EventoCanal empieza(long tick, int altura, int velocidad) {
        return new EventoCanal(tick, EventoCanal.NOTA_EMPIEZA, 0, altura, velocidad);
    }

    private static EventoCanal acaba(long tick, int altura) {
        return new EventoCanal(tick, EventoCanal.NOTA_ACABA, 0, altura, 64);
    }

    private static EventoMeta meta(long tick, int tipo, int... datos) {
        byte[] b = new byte[datos.length];
        for (int i = 0; i < datos.length; i++) {
            b[i] = (byte) datos[i];
        }
        return new EventoMeta(tick, tipo, b);
    }

    private static EventoMeta fin(long tick) {
        return meta(tick, EventoMeta.FIN_DE_PISTA);
    }

    private static List<Nota> notas(int ticksPorNegra, EventoMidi... eventos) {
        return ConversorPista.convertir(List.of(eventos), ticksPorNegra).pista().notas();
    }

    // --- Notas ---

    @Test
    void emparejaElInicioYElFinalDeUnaNota() {
        List<Nota> notas = notas(960, empieza(0, 60, 96), acaba(480, 60), fin(480));

        assertEquals(List.of(new Nota(60, 0, 480, 96, 0)), notas);
    }

    @Test
    void unaNotaQueEmpiezaConVelocidadCeroEsUnFinal() {
        // Muchos programas terminan las notas con "nota empieza" y velocidad 0 en vez de "nota acaba".
        List<Nota> notas = notas(960, empieza(0, 60, 96), empieza(960, 60, 0), fin(960));

        assertEquals(List.of(new Nota(60, 0, 960, 96, 0)), notas);
    }

    @Test
    void pasaLosTicksA960PorNegra() {
        // FL Studio exporta a 96 ticks por negra: 48 ticks = media negra = 480 en Falls.
        List<Nota> notas = notas(96, empieza(48, 60, 96), acaba(96, 60), fin(96));

        assertEquals(List.of(new Nota(60, 480, 480, 96, 0)), notas);
    }

    @Test
    void alPasarA960RedondeaAlTickMasCercano() {
        // Con 7 ticks por negra, 1 tick = 960 / 7 = 137,14... -> 137; 4 ticks = 548,57... -> 549
        List<Nota> notas = notas(7, empieza(1, 60, 96), acaba(4, 60), fin(4));

        assertEquals(137, notas.get(0).inicio());
        assertEquals(549 - 137, notas.get(0).duracion());
    }

    @Test
    void leeAcordesConVariasNotasALaVez() {
        List<Nota> notas = notas(960,
                empieza(0, 60, 90), empieza(0, 64, 90), empieza(0, 67, 90),
                acaba(960, 64), acaba(1920, 60), acaba(1920, 67), fin(1920));

        assertEquals(List.of(
                new Nota(60, 0, 1920, 90, 0),
                new Nota(64, 0, 960, 90, 0),
                new Nota(67, 0, 1920, 90, 0)), notas);
    }

    @Test
    void lasNotasSalenOrdenadasPorInicioYAltura() {
        List<Nota> notas = notas(960,
                empieza(480, 62, 80), empieza(0, 67, 80), empieza(0, 60, 80),
                acaba(600, 62), acaba(960, 60), acaba(960, 67), fin(960));

        assertEquals(List.of(60, 67, 62), notas.stream().map(Nota::altura).toList());
    }

    @Test
    void laMismaNotaSolapadaSeEmparejaEnOrden() {
        // Do4 suena dos veces solapado: el primer final cierra la primera nota (primero en entrar, primero en salir).
        List<Nota> notas = notas(960,
                empieza(0, 60, 100), empieza(240, 60, 70),
                acaba(480, 60), acaba(720, 60), fin(720));

        assertEquals(List.of(
                new Nota(60, 0, 480, 100, 0),
                new Nota(60, 240, 480, 70, 0)), notas);
    }

    @Test
    void lasNotasDeCanalesDistintosNoSeMezclan() {
        List<Nota> notas = notas(960,
                new EventoCanal(0, EventoCanal.NOTA_EMPIEZA, 0, 60, 90),
                new EventoCanal(0, EventoCanal.NOTA_EMPIEZA, 1, 60, 90),
                new EventoCanal(240, EventoCanal.NOTA_ACABA, 1, 60, 0),
                new EventoCanal(480, EventoCanal.NOTA_ACABA, 0, 60, 0),
                fin(480));

        assertEquals(List.of(
                new Nota(60, 0, 480, 90, 0),
                new Nota(60, 0, 240, 90, 1)), notas);
    }

    @Test
    void unFinalSinInicioSeIgnora() {
        List<Nota> notas = notas(960, acaba(0, 72), empieza(0, 60, 96), acaba(480, 60), fin(480));

        assertEquals(List.of(new Nota(60, 0, 480, 96, 0)), notas);
    }

    @Test
    void unaNotaSinFinalSeCierraAlAcabarLaPista() {
        List<Nota> notas = notas(960, empieza(0, 60, 96), fin(1920));

        assertEquals(List.of(new Nota(60, 0, 1920, 96, 0)), notas);
    }

    @Test
    void sinFinDePistaSeCierraEnElUltimoEvento() {
        // La nota 61 empieza en el último evento: duraría 0, así que dura el mínimo, 1 tick.
        List<Nota> notas = notas(960, empieza(0, 60, 96), empieza(480, 61, 96));

        assertEquals(List.of(
                new Nota(60, 0, 480, 96, 0),
                new Nota(61, 480, 1, 96, 0)), notas);
    }

    @Test
    void unaNotaDeDuracionCeroDuraUnTick() {
        // Algunos programas exportan golpes de batería con inicio y final en el mismo tick.
        List<Nota> notas = notas(960, empieza(0, 36, 110), acaba(0, 36), fin(960));

        assertEquals(List.of(new Nota(36, 0, 1, 110, 0)), notas);
    }

    // --- Controladores y pitch bend ---

    @Test
    void leeLosControladores() {
        PistaConvertida r = ConversorPista.convertir(List.of(
                new EventoCanal(96, EventoCanal.CONTROLADOR, 0, 64, 127),
                new EventoCanal(192, EventoCanal.CONTROLADOR, 0, 64, 0),
                fin(192)), 96);

        assertEquals(List.of(
                new Controlador(960, 0, Controlador.SUSTAIN, 127),
                new Controlador(1920, 0, Controlador.SUSTAIN, 0)), r.pista().controladores());
    }

    @Test
    void elPitchBendJuntaSusDosDatosConElMenosImportantePrimero() {
        // Al revés que los números del archivo: primero van los 7 bits bajos y luego los 7 altos.
        PistaConvertida r = ConversorPista.convertir(List.of(
                new EventoCanal(0, EventoCanal.PITCH_BEND, 0, 0x00, 0x40),
                new EventoCanal(0, EventoCanal.PITCH_BEND, 0, 0x7F, 0x7F),
                new EventoCanal(0, EventoCanal.PITCH_BEND, 0, 0x00, 0x00),
                fin(0)), 960);

        assertEquals(List.of(
                new PitchBend(0, 0, PitchBend.CENTRO),
                new PitchBend(0, 0, 16383),
                new PitchBend(0, 0, 0)), r.pista().bends());
    }

    @Test
    void ignoraLosEventosQueFallsNoUsa() {
        PistaConvertida r = ConversorPista.convertir(List.of(
                new EventoCanal(0, EventoCanal.INSTRUMENTO, 0, 5, 0),
                new EventoCanal(0, EventoCanal.PRESION_CANAL, 0, 40, 0),
                new EventoCanal(0, EventoCanal.PRESION_TECLA, 0, 60, 30),
                meta(0, 0x01, 'h', 'o', 'l', 'a'),
                fin(0)), 960);

        assertTrue(r.pista().notas().isEmpty());
        assertTrue(r.pista().controladores().isEmpty());
        assertTrue(r.pista().bends().isEmpty());
    }

    // --- Metaeventos ---

    @Test
    void leeElNombreDeLaPista() {
        PistaConvertida conNombre = ConversorPista.convertir(
                List.of(meta(0, EventoMeta.NOMBRE_PISTA, 'B', 'a', 'j', 'o'), fin(0)), 960);
        PistaConvertida sinNombre = ConversorPista.convertir(List.of(fin(0)), 960);

        assertEquals("Bajo", conNombre.pista().nombre());
        assertEquals("", sinNombre.pista().nombre());
    }

    @Test
    void leeTempoCompasYTonalidad() {
        PistaConvertida r = ConversorPista.convertir(List.of(
                meta(0, EventoMeta.TEMPO, 0x07, 0xA1, 0x20),        // 500.000 µs = 120 bpm
                meta(0, EventoMeta.COMPAS, 4, 2, 24, 8),             // 4/4 (el denominador va como potencia: 2^2 = 4)
                meta(0, EventoMeta.TONALIDAD, 0xFD, 1),              // -3 = 3 bemoles, 1 = menor: Do menor
                meta(384, EventoMeta.TEMPO, 0x0A, 0x2C, 0x2B),       // 666.667 µs = 90 bpm
                meta(384, EventoMeta.COMPAS, 7, 3, 24, 8),           // 7/8
                fin(384)), 96);

        assertEquals(List.of(new CambioTempo(0, 120.0), CambioTempo.desdeMicrosegundos(3840, 666_667)), r.tempos());
        assertEquals(List.of(new CambioCompas(0, 4, 4), new CambioCompas(3840, 7, 8)), r.compases());
        assertEquals(List.of(new CambioTonalidad(0, -3, true)), r.tonalidades());
    }

    @Test
    void ignoraLosMetaeventosMalFormados() {
        // Mejor ignorar un dato raro que rechazar un archivo entero que por lo demás está bien.
        PistaConvertida r = ConversorPista.convertir(List.of(
                meta(0, EventoMeta.TEMPO, 0x07, 0xA1),          // tempo con 2 bytes en vez de 3
                meta(0, EventoMeta.TEMPO, 0x00, 0x00, 0x00),    // tempo 0: imposible
                meta(0, EventoMeta.COMPAS, 0, 2, 24, 8),        // 0 tiempos por compás
                meta(0, EventoMeta.COMPAS, 4, 40, 24, 8),       // denominador 2^40
                meta(0, EventoMeta.TONALIDAD, 9, 0),            // 9 sostenidos
                meta(0, EventoMeta.TONALIDAD, 0, 2),            // modo 2: ni mayor ni menor
                fin(0)), 960);

        assertTrue(r.tempos().isEmpty());
        assertTrue(r.compases().isEmpty());
        assertTrue(r.tonalidades().isEmpty());
    }

    // --- Rol ---

    @Test
    void unaPistaConTodasLasNotasEnElCanal10EsBateria() {
        // El canal 10 de General MIDI es el 9 contando desde 0.
        PistaConvertida bateria = ConversorPista.convertir(List.of(
                new EventoCanal(0, EventoCanal.NOTA_EMPIEZA, 9, 36, 100),
                new EventoCanal(240, EventoCanal.NOTA_ACABA, 9, 36, 0),
                fin(240)), 960);
        PistaConvertida mezcla = ConversorPista.convertir(List.of(
                new EventoCanal(0, EventoCanal.NOTA_EMPIEZA, 9, 36, 100),
                empieza(0, 60, 90),
                fin(240)), 960);
        PistaConvertida vacia = ConversorPista.convertir(List.of(fin(0)), 960);

        assertEquals(Rol.BATERIA, bateria.pista().rol());
        assertEquals(Rol.DESCONOCIDO, mezcla.pista().rol());
        assertEquals(Rol.DESCONOCIDO, vacia.pista().rol());
    }

    @Test
    void rechazaUnaResolucionDeOrigenImposible() {
        assertThrows(IllegalArgumentException.class, () -> ConversorPista.convertir(List.of(), 0));
    }
}
