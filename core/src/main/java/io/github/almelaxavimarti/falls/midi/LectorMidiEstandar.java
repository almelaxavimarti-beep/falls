package io.github.almelaxavimarti.falls.midi;

import io.github.almelaxavimarti.falls.modelo.CambioCompas;
import io.github.almelaxavimarti.falls.modelo.CambioTempo;
import io.github.almelaxavimarti.falls.modelo.CambioTonalidad;
import io.github.almelaxavimarti.falls.modelo.Cancion;
import io.github.almelaxavimarti.falls.modelo.Pista;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Lector de archivos MIDI estándar (formatos 0 y 1), escrito desde cero (decisión 012). */
public final class LectorMidiEstandar implements LectorMidi {

    @Override
    public Cancion leer(byte[] datos) throws ArchivoMidiCorruptoException {
        LectorBytes lector = new LectorBytes(datos);
        CabeceraMidi cabecera = CabeceraMidi.leer(lector);

        // 1. Leer las pistas que dice la cabecera, saltando los bloques desconocidos
        List<List<EventoMidi>> pistasLeidas = new ArrayList<>();
        while (pistasLeidas.size() < cabecera.numeroPistas()) {
            int inicioBloque = lector.posicion();
            if (lector.restantes() < 8) {
                throw new ArchivoMidiCorruptoException("Faltan pistas: la cabecera dice "
                        + cabecera.numeroPistas() + " y solo hay " + pistasLeidas.size(), inicioBloque);
            }
            if (lector.mirarTexto(4).equals("MTrk")) {
                pistasLeidas.add(LectorPista.leer(lector));
            } else {
                // La especificación dice que los bloques que no se conocen se saltan
                lector.saltar(4);
                long longitud = lector.leerUint32();
                if (longitud > lector.restantes()) {
                    throw new ArchivoMidiCorruptoException("Bloque desconocido más largo que el archivo",
                            inicioBloque);
                }
                lector.saltar((int) longitud);
            }
        }

        // 2. En formato 0 todo va en una pista: se separa por canales
        if (cabecera.formato() == 0) {
            pistasLeidas = separarPorCanal(pistasLeidas.get(0));
        }

        // 3. Convertir cada pista y juntar los cambios de tempo, compás y tonalidad
        List<Pista> pistas = new ArrayList<>();
        List<CambioTempo> tempos = new ArrayList<>();
        List<CambioCompas> compases = new ArrayList<>();
        List<CambioTonalidad> tonalidades = new ArrayList<>();

        for (List<EventoMidi> eventos : pistasLeidas) {
            PistaConvertida convertida = ConversorPista.convertir(eventos, cabecera.ticksPorNegra());
            tempos.addAll(convertida.tempos());
            compases.addAll(convertida.compases());
            tonalidades.addAll(convertida.tonalidades());

            Pista pista = convertida.pista();
            boolean tieneMusica = !pista.notas().isEmpty() || !pista.controladores().isEmpty()
                    || !pista.bends().isEmpty();
            if (tieneMusica) {
                pistas.add(pista);
            }
        }

        // sort es estable: si dos cambios caen en el mismo tick, se respeta el orden del archivo
        tempos.sort(Comparator.comparingLong(CambioTempo::tick));
        compases.sort(Comparator.comparingLong(CambioCompas::tick));
        tonalidades.sort(Comparator.comparingLong(CambioTonalidad::tick));

        return new Cancion(tempos, compases, tonalidades, pistas);
    }

    /**
     * Separa la única pista de un archivo de formato 0 en una pista por canal.
     * Los metaeventos (tempo, compás, nombre...) se quedan en una pista aparte.
     */
    private static List<List<EventoMidi>> separarPorCanal(List<EventoMidi> eventos) {
        List<EventoMidi> metaeventos = new ArrayList<>();
        TreeMap<Integer, List<EventoMidi>> porCanal = new TreeMap<>();

        for (EventoMidi evento : eventos) {
            if (evento instanceof EventoCanal e) {
                porCanal.computeIfAbsent(e.canal(), k -> new ArrayList<>()).add(e);
            } else {
                metaeventos.add(evento);
            }
        }

        // Con un solo canal no hace falta separar nada: se deja como estaba (con su nombre)
        if (porCanal.size() <= 1) {
            return List.of(eventos);
        }

        long tickFinal = eventos.get(eventos.size() - 1).tick();
        List<List<EventoMidi>> resultado = new ArrayList<>();
        resultado.add(metaeventos);
        for (Map.Entry<Integer, List<EventoMidi>> entrada : porCanal.entrySet()) {
            List<EventoMidi> pista = new ArrayList<>();
            String nombre = "Canal " + (entrada.getKey() + 1);
            pista.add(new EventoMeta(0, EventoMeta.NOMBRE_PISTA, nombre.getBytes(StandardCharsets.US_ASCII)));
            pista.addAll(entrada.getValue());
            // El final de la pista original, para cerrar las notas que no acaben
            pista.add(new EventoMeta(tickFinal, EventoMeta.FIN_DE_PISTA, new byte[0]));
            resultado.add(pista);
        }
        return resultado;
    }
}