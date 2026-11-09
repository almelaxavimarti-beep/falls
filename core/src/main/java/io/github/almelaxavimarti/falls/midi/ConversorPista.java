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
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Convierte los eventos crudos de una pista MIDI en el modelo de Falls. */
public final class ConversorPista {

    private ConversorPista() {
    }

    /** Una nota que ha empezado y todavía no ha acabado (con los ticks del archivo original). */
    private record NotaAbierta(long inicio, int velocidad) {
    }

    public static PistaConvertida convertir(List<EventoMidi> eventos, int ticksPorNegraOrigen) {
        if (ticksPorNegraOrigen <= 0) {
            throw new IllegalArgumentException("Ticks por negra imposibles: " + ticksPorNegraOrigen);
        }

        List<Nota> notas = new ArrayList<>();
        List<Controlador> controladores = new ArrayList<>();
        List<PitchBend> bends = new ArrayList<>();
        List<CambioTempo> tempos = new ArrayList<>();
        List<CambioCompas> compases = new ArrayList<>();
        List<CambioTonalidad> tonalidades = new ArrayList<>();
        String nombre = null;

        // Notas abiertas por canal y altura. Una cola: la primera que empieza es la primera que acaba.
        Map<Integer, Deque<NotaAbierta>> abiertas = new HashMap<>();
        long ultimoTick = 0;

        for (EventoMidi evento : eventos) {
            ultimoTick = Math.max(ultimoTick, evento.tick());
            long tick = escalar(evento.tick(), ticksPorNegraOrigen);

            if (evento instanceof EventoCanal e) {
                int clave = e.canal() * 128 + e.dato1();
                boolean empieza = e.tipo() == EventoCanal.NOTA_EMPIEZA && e.dato2() > 0;
                boolean acaba = e.tipo() == EventoCanal.NOTA_ACABA
                        || (e.tipo() == EventoCanal.NOTA_EMPIEZA && e.dato2() == 0);

                if (empieza) {
                    abiertas.computeIfAbsent(clave, k -> new ArrayDeque<>())
                            .addLast(new NotaAbierta(e.tick(), e.dato2()));
                } else if (acaba) {
                    Deque<NotaAbierta> cola = abiertas.get(clave);
                    if (cola != null && !cola.isEmpty()) {
                        NotaAbierta abierta = cola.removeFirst();
                        notas.add(crearNota(e.dato1(), e.canal(), abierta, e.tick(), ticksPorNegraOrigen));
                    }
                } else if (e.tipo() == EventoCanal.CONTROLADOR) {
                    controladores.add(new Controlador(tick, e.canal(), e.dato1(), e.dato2()));
                } else if (e.tipo() == EventoCanal.PITCH_BEND) {
                    bends.add(new PitchBend(tick, e.canal(), e.dato1() | (e.dato2() << 7)));
                }
            } else if (evento instanceof EventoMeta m) {
                byte[] d = m.datos();
                switch (m.tipo()) {
                    case EventoMeta.NOMBRE_PISTA -> {
                        if (nombre == null) {
                            nombre = new String(d, StandardCharsets.ISO_8859_1);
                        }
                    }
                    case EventoMeta.TEMPO -> {
                        if (d.length == 3) {
                            int us = ((d[0] & 0xFF) << 16) | ((d[1] & 0xFF) << 8) | (d[2] & 0xFF);
                            if (us > 0) {
                                tempos.add(CambioTempo.desdeMicrosegundos(tick, us));
                            }
                        }
                    }
                    case EventoMeta.COMPAS -> {
                        if (d.length >= 2) {
                            int numerador = d[0] & 0xFF;
                            int potencia = d[1] & 0xFF;
                            if (numerador >= 1 && potencia <= 30) {
                                compases.add(new CambioCompas(tick, numerador, 1 << potencia));
                            }
                        }
                    }
                    case EventoMeta.TONALIDAD -> {
                        if (d.length == 2) {
                            int alteraciones = d[0];          // con signo: -7 a 7
                            int modo = d[1] & 0xFF;
                            if (alteraciones >= -7 && alteraciones <= 7 && (modo == 0 || modo == 1)) {
                                tonalidades.add(new CambioTonalidad(tick, alteraciones, modo == 1));
                            }
                        }
                    }
                    default -> {
                        // Otros metaeventos (textos, letras, marcadores...): Falls no los usa todavía.
                    }
                }
            }
        }

        // Notas que no han acabado: se cierran donde acaba la pista
        for (Map.Entry<Integer, Deque<NotaAbierta>> entrada : abiertas.entrySet()) {
            int canal = entrada.getKey() / 128;
            int altura = entrada.getKey() % 128;
            for (NotaAbierta abierta : entrada.getValue()) {
                notas.add(crearNota(altura, canal, abierta, ultimoTick, ticksPorNegraOrigen));
            }
        }

        notas.sort(Comparator.comparingLong(Nota::inicio)
                .thenComparingInt(Nota::altura)
                .thenComparingInt(Nota::canal));

        Pista pista = new Pista(nombre == null ? "" : nombre, deducirRol(notas), notas, controladores, bends);
        return new PistaConvertida(pista, tempos, compases, tonalidades);
    }

    /** Pasa un tick del archivo a la resolución de Falls (960 por negra), redondeando. */
    private static long escalar(long tick, int ticksPorNegraOrigen) {
        return (tick * Cancion.TICKS_POR_NEGRA + ticksPorNegraOrigen / 2) / ticksPorNegraOrigen;
    }

    private static Nota crearNota(int altura, int canal, NotaAbierta abierta, long tickFinal, int origen) {
        long inicio = escalar(abierta.inicio(), origen);
        long duracion = Math.max(1, escalar(tickFinal, origen) - inicio);
        return new Nota(altura, inicio, duracion, abierta.velocidad(), canal);
    }

    /** Primera regla de la decisión 004: si todas las notas van por el canal 10, es una batería. */
    private static Rol deducirRol(List<Nota> notas) {
        if (!notas.isEmpty() && notas.stream().allMatch(n -> n.canal() == 9)) {
            return Rol.BATERIA;
        }
        return Rol.DESCONOCIDO;
    }
}