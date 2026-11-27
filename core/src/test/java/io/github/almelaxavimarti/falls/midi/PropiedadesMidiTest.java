package io.github.almelaxavimarti.falls.midi;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.almelaxavimarti.falls.modelo.Cancion;
import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Tests de propiedades (decisión 017): en vez de comprobar unos pocos casos escritos a mano,
 * se comprueba una regla que tiene que cumplirse siempre, con cientos de canciones al azar.
 * Si uno falla, el mensaje dice la semilla para poder repetir exactamente ese caso.
 */
class PropiedadesMidiTest {

    private static final int CANCIONES = 500;

    private final EscritorMidi escritor = new EscritorMidiEstandar();
    private final LectorMidi lector = new LectorMidiEstandar();

    @Test
    void escribirYReleerDaSiempreLaMismaCancion() throws ArchivoMidiCorruptoException {
        for (long semilla = 0; semilla < CANCIONES; semilla++) {
            Cancion original = new GeneradorCanciones(semilla).cancion();

            Cancion releida = lector.leer(escritor.escribir(original));

            assertEquals(original, releida, "semilla " + semilla);
        }
    }

    @Test
    void escribirLoReleidoDaLosMismosBytes() throws ArchivoMidiCorruptoException {
        for (long semilla = 0; semilla < CANCIONES; semilla++) {
            byte[] primera = escritor.escribir(new GeneradorCanciones(semilla).cancion());

            byte[] segunda = escritor.escribir(lector.leer(primera));

            assertArrayEquals(primera, segunda, "semilla " + semilla);
        }
    }

    @Test
    void conUnArchivoEstropeadoElLectorSoloLanzaArchivoMidiCorrupto() {
        // Decisión 014: ante un archivo roto, un error claro; nunca otra excepción ni un cuelgue.
        for (long semilla = 0; semilla < 2 * CANCIONES; semilla++) {
            byte[] archivo = escritor.escribir(new GeneradorCanciones(semilla).cancion());
            byte[] estropeado = estropear(archivo, new Random(semilla));

            try {
                lector.leer(estropeado);
            } catch (ArchivoMidiCorruptoException e) {
                // Bien: es el error que esperamos para un archivo roto.
            } catch (RuntimeException e) {
                throw new AssertionError("semilla " + semilla + ": el lector lanzó " + e, e);
            }
        }
    }

    /** Cambia unos bytes al azar, corta el archivo, o las dos cosas. */
    private static byte[] estropear(byte[] archivo, Random azar) {
        byte[] copia = archivo.clone();
        int forma = azar.nextInt(3);
        if (forma != 1) {
            int cambios = 1 + azar.nextInt(4);
            for (int i = 0; i < cambios; i++) {
                copia[azar.nextInt(copia.length)] = (byte) azar.nextInt(256);
            }
        }
        if (forma != 0) {
            copia = Arrays.copyOf(copia, azar.nextInt(copia.length));
        }
        return copia;
    }
}
