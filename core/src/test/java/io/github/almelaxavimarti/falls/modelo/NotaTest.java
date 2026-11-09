package io.github.almelaxavimarti.falls.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Una nota: altura MIDI (0-127, 60 = Do4), inicio y duración en ticks (960 por negra),
 * velocidad (1-127) y canal MIDI (0-15).
 */
class NotaTest {

    @Test
    void guardaSusDatos() {
        Nota nota = new Nota(60, 0, 960, 100, 0);

        assertEquals(60, nota.altura());
        assertEquals(0, nota.inicio());
        assertEquals(960, nota.duracion());
        assertEquals(100, nota.velocidad());
        assertEquals(0, nota.canal());
    }

    @Test
    void calculaDondeTermina() {
        Nota nota = new Nota(64, 1920, 480, 90, 0);

        assertEquals(2400, nota.fin());
    }

    @Test
    void aceptaLosLimites() {
        new Nota(0, 0, 1, 1, 0);
        new Nota(127, 0, 1, 127, 15);
    }

    @Test
    void rechazaAlturaFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> new Nota(-1, 0, 960, 100, 0));
        assertThrows(IllegalArgumentException.class, () -> new Nota(128, 0, 960, 100, 0));
    }

    @Test
    void rechazaInicioNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new Nota(60, -1, 960, 100, 0));
    }

    @Test
    void rechazaDuracionCeroONegativa() {
        assertThrows(IllegalArgumentException.class, () -> new Nota(60, 0, 0, 100, 0));
        assertThrows(IllegalArgumentException.class, () -> new Nota(60, 0, -5, 100, 0));
    }

    @Test
    void rechazaVelocidadFueraDeRango() {
        // En MIDI, velocidad 0 significa "fin de nota": una nota que suena tiene al menos 1.
        assertThrows(IllegalArgumentException.class, () -> new Nota(60, 0, 960, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Nota(60, 0, 960, 128, 0));
    }

    @Test
    void rechazaCanalFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> new Nota(60, 0, 960, 100, -1));
        assertThrows(IllegalArgumentException.class, () -> new Nota(60, 0, 960, 100, 16));
    }
}
