package io.github.almelaxavimarti.falls.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Cambios de tempo, compás y tonalidad, controladores y pitch bend. */
class EventosTest {

    // --- CambioTempo(tick, bpm) ---

    @Test
    void tempoGuardaSusDatos() {
        SCambioTempo tempo = new CambioTempo(0, 120.0);

        assertEquals(0, tempo.tick());
        assertEquals(120.0, tempo.bpm());
    }

    @Test
    void tempoSeConvierteDesdeYHaciaMicrosegundos() {
        // Los MIDI guardan el tempo como microsegundos por negra: 500.000 µs = 120 bpm.
        assertEquals(120.0, CambioTempo.desdeMicrosegundos(0, 500_000).bpm(), 1e-9);
        assertEquals(500_000, new CambioTempo(0, 120.0).microsegundosPorNegra());
        assertEquals(666_667, new CambioTempo(0, 90.0).microsegundosPorNegra());
    }

    @Test
    void tempoRechazaValoresImposibles() {
        assertThrows(IllegalArgumentException.class, () -> new CambioTempo(-1, 120.0));
        assertThrows(IllegalArgumentException.class, () -> new CambioTempo(0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new CambioTempo(0, -60.0));
        assertThrows(IllegalArgumentException.class, () -> CambioTempo.desdeMicrosegundos(0, 0));
    }

    // --- CambioCompas(tick, numerador, denominador) ---

    @Test
    void compasAceptaCompasesHabituales() {
        CambioCompas cuatroPorCuatro = new CambioCompas(0, 4, 4);
        assertEquals(4, cuatroPorCuatro.numerador());
        assertEquals(4, cuatroPorCuatro.denominador());

        new CambioCompas(3840, 7, 8);
        new CambioCompas(0, 6, 8);
        new CambioCompas(0, 2, 2);
        new CambioCompas(0, 3, 16);
    }

    @Test
    void compasRechazaValoresImposibles() {
        assertThrows(IllegalArgumentException.class, () -> new CambioCompas(-1, 4, 4));
        assertThrows(IllegalArgumentException.class, () -> new CambioCompas(0, 0, 4));
        // El denominador es la figura que vale un tiempo: tiene que ser potencia de 2 (1, 2, 4, 8, 16...).
        assertThrows(IllegalArgumentException.class, () -> new CambioCompas(0, 4, 3));
        assertThrows(IllegalArgumentException.class, () -> new CambioCompas(0, 4, 0));
    }

    // --- CambioTonalidad(tick, alteraciones, menor) ---

    @Test
    void tonalidadGuardaSusDatos() {
        // MIDI guarda la armadura como número de alteraciones: negativo = bemoles, positivo = sostenidos.
        CambioTonalidad laMenor = new CambioTonalidad(0, 0, true);
        CambioTonalidad miBemolMayor = new CambioTonalidad(0, -3, false);

        assertEquals(0, laMenor.alteraciones());
        assertEquals(true, laMenor.menor());
        assertEquals(-3, miBemolMayor.alteraciones());
    }

    @Test
    void tonalidadRechazaArmadurasImposibles() {
        assertThrows(IllegalArgumentException.class, () -> new CambioTonalidad(-1, 0, false));
        assertThrows(IllegalArgumentException.class, () -> new CambioTonalidad(0, 8, false));
        assertThrows(IllegalArgumentException.class, () -> new CambioTonalidad(0, -8, false));
    }

    // --- Controlador(tick, canal, numero, valor) ---

    @Test
    void controladorGuardaSusDatos() {
        Controlador pedal = new Controlador(960, 0, Controlador.SUSTAIN, 127);

        assertEquals(64, Controlador.SUSTAIN);
        assertEquals(960, pedal.tick());
        assertEquals(127, pedal.valor());
    }

    @Test
    void controladorRechazaValoresFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> new Controlador(-1, 0, 64, 0));
        assertThrows(IllegalArgumentException.class, () -> new Controlador(0, 16, 64, 0));
        assertThrows(IllegalArgumentException.class, () -> new Controlador(0, 0, 128, 0));
        assertThrows(IllegalArgumentException.class, () -> new Controlador(0, 0, 64, 128));
        assertThrows(IllegalArgumentException.class, () -> new Controlador(0, 0, -1, 0));
    }

    // --- PitchBend(tick, canal, valor) ---

    @Test
    void pitchBendTieneSuCentroEn8192() {
        // 0 = abajo del todo, 8192 = sin bend, 16383 = arriba del todo.
        assertEquals(8192, PitchBend.CENTRO);
        new PitchBend(0, 0, 0);
        new PitchBend(0, 0, 16383);
    }

    @Test
    void pitchBendRechazaValoresFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> new PitchBend(-1, 0, 8192));
        assertThrows(IllegalArgumentException.class, () -> new PitchBend(0, -1, 8192));
        assertThrows(IllegalArgumentException.class, () -> new PitchBend(0, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> new PitchBend(0, 0, 16384));
    }
}
