package io.github.almelaxavimarti.falls.midi;

import java.util.ArrayList;
import java.util.List;

/** Lee una pista ("MTrk") de un archivo MIDI y devuelve sus eventos con el tick absoluto. */
public final class LectorPista {

    private LectorPista() {
    }

    public static List<EventoMidi> leer(LectorBytes lector) throws ArchivoMidiCorruptoException {
        // 1. Cabecera de la pista: "MTrk" y cuántos bytes ocupa
        int inicio = lector.posicion();
        if (lector.restantes() < 4 || !lector.leerTexto(4).equals("MTrk")) {
            throw new ArchivoMidiCorruptoException("Se esperaba una pista (MTrk)", inicio);
        }
        int posLongitud = lector.posicion();
        long longitud = lector.leerUint32();
        if (longitud > lector.restantes()) {
            throw new ArchivoMidiCorruptoException("La pista dice medir " + longitud
                    + " bytes, pero el archivo se acaba antes", posLongitud);
        }
        int fin = lector.posicion() + (int) longitud;

        // 2. Eventos, uno detrás de otro, hasta el final de la pista
        List<EventoMidi> eventos = new ArrayList<>();
        long tick = 0;
        int ultimoEstado = 0;

        while (lector.posicion() < fin) {
            tick += lector.leerCantidadVariable();
            int posEvento = lector.posicion();
            int b = lector.leerUint8();

            if (b == 0xFF) {
                // Metaevento: tipo, longitud y datos
                int tipo = lector.leerUint8();
                int largo = lector.leerCantidadVariable();
                byte[] datos = lector.leerBytes(largo);
                eventos.add(new EventoMeta(tick, tipo, datos));
                if (tipo == EventoMeta.FIN_DE_PISTA) {
                    break;
                }
            } else if (b == 0xF0 || b == 0xF7) {
                // SysEx: Falls no lo usa, se salta. Anula el running status.
                int largo = lector.leerCantidadVariable();
                lector.saltar(largo);
                ultimoEstado = 0;
            } else if (b >= 0xF0) {
                throw new ArchivoMidiCorruptoException("Estado de sistema no válido en un archivo", posEvento);
            } else {
                // Evento de canal, con o sin running status
                int estado;
                int dato1;
                if (b >= 0x80) {
                    estado = b;
                    dato1 = leerDato(lector);
                } else {
                    if (ultimoEstado == 0) {
                        throw new ArchivoMidiCorruptoException("Running status sin evento anterior", posEvento);
                    }
                    estado = ultimoEstado;
                    dato1 = b;
                }
                ultimoEstado = estado;

                int tipo = estado & 0xF0;
                int canal = estado & 0x0F;
                int dato2 = 0;
                if (tipo != EventoCanal.INSTRUMENTO && tipo != EventoCanal.PRESION_CANAL) {
                    dato2 = leerDato(lector);
                }
                eventos.add(new EventoCanal(tick, tipo, canal, dato1, dato2));
            }

            if (lector.posicion() > fin) {
                throw new ArchivoMidiCorruptoException("Un evento se sale del final de la pista", posEvento);
            }
        }

        // 3. Saltar lo que sobre tras el fin de pista
        lector.saltar(fin - lector.posicion());
        return eventos;
    }

    /** Lee un byte de datos de un evento de canal: tiene que estar entre 0 y 127. */
    private static int leerDato(LectorBytes lector) throws ArchivoMidiCorruptoException {
        int pos = lector.posicion();
        int dato = lector.leerUint8();
        if (dato > 127) {
            throw new ArchivoMidiCorruptoException("Byte de datos fuera de rango (0-127): " + dato, pos);
        }
        return dato;
    }
}

