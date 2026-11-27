package io.github.almelaxavimarti.falls.midi;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Casos concretos de archivos rotos que encontraron los tests de propiedades (PropiedadesMidiTest).
 * Se dejan escritos a mano para que no vuelvan a aparecer.
 */
class ArchivosEstropeadosTest {

    private static LectorBytes lector(int... valores) {
        byte[] datos = new byte[valores.length];
        for (int i = 0; i < valores.length; i++) {
            datos[i] = (byte) valores[i];
        }
        return new LectorBytes(datos);
    }

    @Test
    void unaCabeceraQueDiceMedirMasQueElArchivoEsUnError() {
        // Encontrado con la semilla 38: la longitud 0xFFFFFFF0 pasada a int daba un número negativo
        // y el lector retrocedía en vez de avanzar.
        ArchivoMidiCorruptoException error = assertThrows(ArchivoMidiCorruptoException.class,
                () -> CabeceraMidi.leer(lector('M', 'T', 'h', 'd', 0xFF, 0xFF, 0xFF, 0xF0,
                        0, 1, 0, 1, 0x00, 0x60)));

        assertTrue(error.getMessage().contains("dice medir"));
    }

    @Test
    void unFinDePistaQueSeSaleDeLaPistaEsUnError() {
        // Encontrado con la semilla 1403: la pista dice medir 3 bytes, pero el fin de pista ocupa 4.
        // Antes el break se saltaba la comprobación y el lector retrocedía 1 byte.
        ArchivoMidiCorruptoException error = assertThrows(ArchivoMidiCorruptoException.class,
                () -> LectorPista.leer(lector('M', 'T', 'r', 'k', 0, 0, 0, 3,
                        0x00, 0xFF, 0x2F, 0x00)));

        assertTrue(error.getMessage().contains("se sale del final de la pista"));
    }

    @Test
    void elLectorDeBytesNoAceptaCantidadesNegativas() {
        // Una cantidad negativa nunca viene del archivo: sería un fallo de Falls. Se para en seco.
        LectorBytes lector = lector(1, 2, 3, 4);

        assertThrows(IllegalArgumentException.class, () -> lector.saltar(-1));
        assertThrows(IllegalArgumentException.class, () -> lector.leerBytes(-1));
        assertThrows(IllegalArgumentException.class, () -> lector.leerTexto(-1));
    }
}
