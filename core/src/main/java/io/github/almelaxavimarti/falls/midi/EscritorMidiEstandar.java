package io.github.almelaxavimarti.falls.midi;

import io.github.almelaxavimarti.falls.modelo.CambioCompas;
import io.github.almelaxavimarti.falls.modelo.CambioTempo;
import io.github.almelaxavimarti.falls.modelo.CambioTonalidad;
import io.github.almelaxavimarti.falls.modelo.Cancion;
import io.github.almelaxavimarti.falls.modelo.Controlador;
import io.github.almelaxavimarti.falls.modelo.Nota;
import io.github.almelaxavimarti.falls.modelo.PitchBend;
import io.github.almelaxavimarti.falls.modelo.Pista;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Escritor MIDI propio (decisión 012). Siempre escribe formato 1 a 960 ticks por negra:
 * una primera pista con los tempos, compases y tonalidades, y después una pista por cada
 * {@link Pista} de la canción.
 */
public final class EscritorMidiEstandar implements EscritorMidi {

    // Orden de los eventos que caen en el mismo tick: primero los metaeventos, después las
    // notas que acaban, los controladores, el pitch bend y por último las notas que empiezan.
    // Así una nota que acaba justo donde empieza otra de la misma altura no se corta.
    private static final int ORDEN_META = 0;
    private static final int ORDEN_NOTA_ACABA = 1;
    private static final int ORDEN_CONTROLADOR = 2;
    private static final int ORDEN_PITCH_BEND = 3;
    private static final int ORDEN_NOTA_EMPIEZA = 4;

    /** Un evento listo para escribir: en qué tick va, en qué orden dentro del tick y sus bytes. */
    private record EventoParaEscribir(long tick, int orden, byte[] bytes) {
    }

    @Override
    public byte[] escribir(Cancion cancion) {
        Objects.requireNonNull(cancion, "La canción no puede ser null");

        EscritorBytes archivo = new EscritorBytes();
        archivo.escribirTexto("MThd");
        archivo.escribirUint32(6);                              // la cabecera siempre mide 6 bytes
        archivo.escribirUint16(1);                              // formato 1
        archivo.escribirUint16(1 + cancion.pistas().size());    // pista de tempo + las de música
        archivo.escribirUint16(Cancion.TICKS_POR_NEGRA);

        escribirPista(archivo, eventosDeTempo(cancion));
        for (Pista pista : cancion.pistas()) {
            escribirPista(archivo, eventosDeMusica(pista));
        }
        return archivo.aBytes();
    }

    /** La primera pista: tempos, compases y tonalidades. */
    private static List<EventoParaEscribir> eventosDeTempo(Cancion cancion) {
        List<EventoParaEscribir> eventos = new ArrayList<>();
        for (CambioTempo tempo : cancion.tempos()) {
            long us = Math.round(60_000_000.0 / tempo.bpm());
            if (us < 1 || us > 0xFF_FFFF) {
                throw new IllegalArgumentException("Tempo imposible de guardar en MIDI: " + tempo.bpm() + " bpm");
            }
            eventos.add(meta(tempo.tick(), EventoMeta.TEMPO,
                    (int) (us >> 16) & 0xFF, (int) (us >> 8) & 0xFF, (int) us & 0xFF));
        }
        for (CambioCompas compas : cancion.compases()) {
            if (compas.numerador() > 255) {
                throw new IllegalArgumentException("Numerador demasiado grande para MIDI: " + compas.numerador());
            }
            int potencia = Integer.numberOfTrailingZeros(compas.denominador());   // 8 = 2^3 -> 3
            eventos.add(meta(compas.tick(), EventoMeta.COMPAS, compas.numerador(), potencia, 24, 8));
        }
        for (CambioTonalidad tonalidad : cancion.tonalidades()) {
            eventos.add(meta(tonalidad.tick(), EventoMeta.TONALIDAD,
                    tonalidad.alteraciones() & 0xFF, tonalidad.menor() ? 1 : 0));
        }
        return eventos;
    }

    /** Una pista de música: nombre, notas, controladores y pitch bend. */
    private static List<EventoParaEscribir> eventosDeMusica(Pista pista) {
        List<EventoParaEscribir> eventos = new ArrayList<>();
        if (!pista.nombre().isEmpty()) {
            byte[] nombre = pista.nombre().getBytes(StandardCharsets.ISO_8859_1);
            eventos.add(meta(0, EventoMeta.NOMBRE_PISTA, nombre));
        }
        for (Nota nota : pista.notas()) {
            eventos.add(canal(nota.inicio(), ORDEN_NOTA_EMPIEZA,
                    EventoCanal.NOTA_EMPIEZA | nota.canal(), nota.altura(), nota.velocidad()));
            eventos.add(canal(nota.fin(), ORDEN_NOTA_ACABA,
                    EventoCanal.NOTA_ACABA | nota.canal(), nota.altura(), 64));
        }
        for (Controlador c : pista.controladores()) {
            eventos.add(canal(c.tick(), ORDEN_CONTROLADOR,
                    EventoCanal.CONTROLADOR | c.canal(), c.numero(), c.valor()));
        }
        for (PitchBend bend : pista.bends()) {
            eventos.add(canal(bend.tick(), ORDEN_PITCH_BEND,
                    EventoCanal.PITCH_BEND | bend.canal(), bend.valor() & 0x7F, bend.valor() >> 7));
        }
        return eventos;
    }

    /** Escribe un bloque MTrk: ordena los eventos, pone los tiempos delta y el fin de pista. */
    private static void escribirPista(EscritorBytes archivo, List<EventoParaEscribir> eventos) {
        eventos.sort(Comparator.comparingLong(EventoParaEscribir::tick)
                .thenComparingInt(EventoParaEscribir::orden));

        EscritorBytes cuerpo = new EscritorBytes();
        long tickAnterior = 0;
        for (EventoParaEscribir evento : eventos) {
            long delta = evento.tick() - tickAnterior;
            if (delta > 0x0FFF_FFFF) {
                throw new IllegalArgumentException("Demasiado tiempo entre dos eventos: " + delta + " ticks");
            }
            cuerpo.escribirCantidadVariable((int) delta);
            cuerpo.escribirBytes(evento.bytes());
            tickAnterior = evento.tick();
        }
        cuerpo.escribirCantidadVariable(0);                       // fin de pista justo después
        cuerpo.escribirBytes(new byte[] {(byte) 0xFF, EventoMeta.FIN_DE_PISTA, 0});

        archivo.escribirTexto("MTrk");
        archivo.escribirUint32(cuerpo.tamano());
        archivo.escribirBytes(cuerpo.aBytes());
    }

    private static EventoParaEscribir canal(long tick, int orden, int estado, int dato1, int dato2) {
        return new EventoParaEscribir(tick, orden, new byte[] {(byte) estado, (byte) dato1, (byte) dato2});
    }

    private static EventoParaEscribir meta(long tick, int tipo, int... datos) {
        byte[] bytes = new byte[datos.length];
        for (int i = 0; i < datos.length; i++) {
            bytes[i] = (byte) datos[i];
        }
        return meta(tick, tipo, bytes);
    }

    /** Un metaevento: FF, tipo, longitud (cantidad variable) y los datos. */
    private static EventoParaEscribir meta(long tick, int tipo, byte[] datos) {
        EscritorBytes e = new EscritorBytes();
        e.escribirUint8(0xFF);
        e.escribirUint8(tipo);
        e.escribirCantidadVariable(datos.length);
        e.escribirBytes(datos);
        return new EventoParaEscribir(tick, ORDEN_META, e.aBytes());
    }
}