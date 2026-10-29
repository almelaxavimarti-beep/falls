package io.github.almelaxavimarti.falls.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Pistas (con su rol) y la canción completa. */
class PistaYCancionTest {

    private static final Nota DO = new Nota(60, 0, 960, 100, 0);
    private static final Nota MI = new Nota(64, 960, 960, 90, 0);

    // --- Rol ---

    @Test
    void existenLosCincoRoles() {
        assertEquals(List.of(Rol.MELODIA, Rol.ACORDES, Rol.BAJO, Rol.BATERIA, Rol.DESCONOCIDO),
                List.of(Rol.values()));
    }

    // --- Pista(nombre, rol, notas, controladores, bends) ---

    @Test
    void pistaGuardaSusDatos() {
        Pista pista = new Pista("Melodia", Rol.MELODIA, List.of(DO, MI), List.of(), List.of());

        assertEquals("Melodia", pista.nombre());
        assertEquals(Rol.MELODIA, pista.rol());
        assertEquals(List.of(DO, MI), pista.notas());
    }

    @Test
    void pistaTieneUnConstructorCortoSoloConNotas() {
        Pista pista = new Pista("Melodia", Rol.MELODIA, List.of(DO));

        assertTrue(pista.controladores().isEmpty());
        assertTrue(pista.bends().isEmpty());
    }

    @Test
    void pistaNoCambiaSiCambiaLaListaOriginal() {
        List<Nota> notas = new ArrayList<>(List.of(DO));
        Pista pista = new Pista("Melodia", Rol.MELODIA, notas);

        notas.add(MI);

        assertEquals(1, pista.notas().size());
    }

    @Test
    void lasListasDeLaPistaNoSePuedenModificar() {
        Pista pista = new Pista("Melodia", Rol.MELODIA, List.of(DO));

        assertThrows(UnsupportedOperationException.class, () -> pista.notas().add(MI));
    }

    @Test
    void pistaAceptaNombreVacioPeroNoNulos() {
        new Pista("", Rol.DESCONOCIDO, List.of());
        assertThrows(NullPointerException.class, () -> new Pista(null, Rol.MELODIA, List.of()));
        assertThrows(NullPointerException.class, () -> new Pista("x", null, List.of()));
        assertThrows(NullPointerException.class, () -> new Pista("x", Rol.MELODIA, null));
    }

    // --- Cancion(tempos, compases, tonalidades, pistas) ---

    @Test
    void laResolucionInternaEs960TicksPorNegra() {
        assertEquals(960, Cancion.TICKS_POR_NEGRA);
    }

    @Test
    void cancionGuardaSusDatos() {
        Pista melodia = new Pista("Melodia", Rol.MELODIA, List.of(DO, MI));
        Cancion cancion = new Cancion(
                List.of(new CambioTempo(0, 120.0), new CambioTempo(15360, 90.0)),
                List.of(new CambioCompas(0, 4, 4)),
                List.of(),
                List.of(melodia));

        assertEquals(2, cancion.tempos().size());
        assertEquals(1, cancion.compases().size());
        assertTrue(cancion.tonalidades().isEmpty());
        assertEquals(List.of(melodia), cancion.pistas());
    }

    @Test
    void lasListasDeLaCancionNoSePuedenModificar() {
        Cancion cancion = new Cancion(List.of(), List.of(), List.of(), List.of());

        assertThrows(UnsupportedOperationException.class,
                () -> cancion.tempos().add(new CambioTempo(0, 120.0)));
        assertThrows(UnsupportedOperationException.class,
                () -> cancion.pistas().add(new Pista("", Rol.DESCONOCIDO, List.of())));
    }

    @Test
    void cancionExigeLosCambiosOrdenadosPorTick() {
        // Si los cambios no están en orden, más adelante se calcularían mal los tiempos.
        assertThrows(IllegalArgumentException.class, () -> new Cancion(
                List.of(new CambioTempo(1920, 90.0), new CambioTempo(0, 120.0)),
                List.of(), List.of(), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new Cancion(
                List.of(),
                List.of(new CambioCompas(3840, 7, 8), new CambioCompas(0, 4, 4)),
                List.of(), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new Cancion(
                List.of(), List.of(),
                List.of(new CambioTonalidad(960, 1, false), new CambioTonalidad(0, 0, false)),
                List.of()));
    }

    @Test
    void cancionRechazaListasNulas() {
        assertThrows(NullPointerException.class, () -> new Cancion(null, List.of(), List.of(), List.of()));
        assertThrows(NullPointerException.class, () -> new Cancion(List.of(), List.of(), List.of(), null));
    }
}
