package io.github.almelaxavimarti.falls.midi;

import io.github.almelaxavimarti.falls.modelo.CambioCompas;
import io.github.almelaxavimarti.falls.modelo.CambioTempo;
import io.github.almelaxavimarti.falls.modelo.CambioTonalidad;
import io.github.almelaxavimarti.falls.modelo.Cancion;
import io.github.almelaxavimarti.falls.modelo.Controlador;
import io.github.almelaxavimarti.falls.modelo.Nota;
import io.github.almelaxavimarti.falls.modelo.PitchBend;
import io.github.almelaxavimarti.falls.modelo.Pista;
import io.github.almelaxavimarti.falls.modelo.Rol;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Inventa canciones al azar, pero siempre válidas y representables en MIDI, para los tests de
 * propiedades (decisión 017). Con la misma semilla sale siempre la misma canción: si un test
 * falla con la semilla 37, se puede repetir exactamente ese caso.
 */
final class GeneradorCanciones {

    private static final String LETRAS = "abcdefghijklmnopqrstuvwxyzáéíóúñÁÉÍÓÚÑ 0123456789-";
    private static final int CANAL_BATERIA = 9;     // el canal 10 de MIDI

    private final Random azar;

    GeneradorCanciones(long semilla) {
        this.azar = new Random(semilla);
    }

    Cancion cancion() {
        List<CambioTempo> tempos = new ArrayList<>();
        for (long tick : ticksOrdenados(entre(0, 3))) {
            // Entre 30 y 300 bpm, en microsegundos por negra como los guarda MIDI
            tempos.add(CambioTempo.desdeMicrosegundos(tick, entre(200_000, 2_000_000)));
        }
        List<CambioCompas> compases = new ArrayList<>();
        for (long tick : ticksOrdenados(entre(0, 3))) {
            compases.add(new CambioCompas(tick, entre(1, 15), 1 << entre(0, 5)));   // /1 a /32
        }
        List<CambioTonalidad> tonalidades = new ArrayList<>();
        for (long tick : ticksOrdenados(entre(0, 2))) {
            tonalidades.add(new CambioTonalidad(tick, entre(-7, 7), azar.nextBoolean()));
        }
        List<Pista> pistas = new ArrayList<>();
        int cantidad = entre(0, 4);
        for (int i = 0; i < cantidad; i++) {
            pistas.add(pista());
        }
        return new Cancion(tempos, compases, tonalidades, pistas);
    }

    private Pista pista() {
        boolean bateria = azar.nextInt(4) == 0;
        // Una pista normal usa uno o dos canales, nunca el de batería (si no, cambiaría de rol).
        int canalA = bateria ? CANAL_BATERIA : canalQueNoEsBateria();
        int canalB = bateria ? CANAL_BATERIA : canalQueNoEsBateria();

        List<Nota> notas = notas(bateria, canalA, canalB);

        List<Controlador> controladores = new ArrayList<>();
        for (long tick : ticksOrdenados(entre(0, 5))) {
            controladores.add(new Controlador(tick, canalA, entre(0, 127), entre(0, 127)));
        }
        List<PitchBend> bends = new ArrayList<>();
        for (long tick : ticksOrdenados(entre(0, 5))) {
            bends.add(new PitchBend(tick, canalA, entre(0, 16383)));
        }
        String nombre = azar.nextBoolean() ? "" : nombre();
        return new Pista(nombre, bateria ? Rol.BATERIA : Rol.DESCONOCIDO, notas, controladores, bends);
    }

    private List<Nota> notas(boolean bateria, int canalA, int canalB) {
        List<Nota> candidatas = new ArrayList<>();
        int cantidad = entre(1, 40);
        for (int i = 0; i < cantidad; i++) {
            int altura = bateria ? entre(35, 81) : entre(0, 127);
            long inicio = entre(0, 50_000);
            long duracion = azar.nextInt(10) == 0 ? entre(1, 200_000) : entre(1, 4_000);
            int canal = azar.nextBoolean() ? canalA : canalB;
            candidatas.add(new Nota(altura, inicio, duracion, entre(1, 127), canal));
        }
        candidatas.sort(Comparator.comparingLong(Nota::inicio));

        // MIDI no distingue dos notas de la misma tecla y canal que se pisan: esas se descartan.
        Map<Integer, Long> finDeLaUltima = new HashMap<>();
        List<Nota> notas = new ArrayList<>();
        for (Nota nota : candidatas) {
            int tecla = nota.canal() * 128 + nota.altura();
            if (nota.inicio() >= finDeLaUltima.getOrDefault(tecla, 0L)) {
                notas.add(nota);
                finDeLaUltima.put(tecla, nota.fin());
            }
        }
        // El mismo orden en que las devuelve el lector
        notas.sort(Comparator.comparingLong(Nota::inicio)
                .thenComparingInt(Nota::altura)
                .thenComparingInt(Nota::canal));
        return notas;
    }

    private String nombre() {
        StringBuilder nombre = new StringBuilder();
        int longitud = entre(1, 12);
        for (int i = 0; i < longitud; i++) {
            nombre.append(LETRAS.charAt(azar.nextInt(LETRAS.length())));
        }
        return nombre.toString();
    }

    private int canalQueNoEsBateria() {
        int canal = entre(0, 14);
        return canal >= CANAL_BATERIA ? canal + 1 : canal;     // 0-8 y 10-15
    }

    private List<Long> ticksOrdenados(int cantidad) {
        List<Long> ticks = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            ticks.add((long) entre(0, 100_000));
        }
        ticks.sort(null);
        return ticks;
    }

    /** Un número al azar entre min y max, los dos incluidos. */
    private int entre(int min, int max) {
        return min + azar.nextInt(max - min + 1);
    }
}
