package io.github.almelaxavimarti.falls.midi;

/**
 * Metaevento: información que no es sonido (tempo, compás, tonalidad, nombre de pista, fin de pista...).
 *
 * @param tipo  el byte que indica qué metaevento es (por ejemplo 0x51 = tempo)
 * @param datos sus bytes, tal cual vienen en el archivo
 */
public record EventoMeta(long tick, int tipo, byte[] datos) implements EventoMidi {

    public static final int NOMBRE_PISTA = 0x03;
    public static final int FIN_DE_PISTA = 0x2F;
    public static final int TEMPO = 0x51;
    public static final int COMPAS = 0x58;
    public static final int TONALIDAD = 0x59;

    public EventoMeta {
        datos = datos.clone();
    }

    @Override
    public byte[] datos() {
        return datos.clone();
    }
}
