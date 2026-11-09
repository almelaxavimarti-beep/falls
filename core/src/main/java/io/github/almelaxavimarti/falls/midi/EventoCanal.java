package io.github.almelaxavimarti.falls.midi;

/**
 * Evento de canal: nota que empieza o acaba, controlador, cambio de instrumento, pitch bend...
 *
 * @param tipo  los 4 bits altos del byte de estado: 0x80 nota acaba, 0x90 nota empieza,
 *              0xA0 presión de tecla, 0xB0 controlador, 0xC0 instrumento, 0xD0 presión de canal,
 *              0xE0 pitch bend
 * @param canal los 4 bits bajos del byte de estado (0-15)
 * @param dato1 primer byte de datos (0-127)
 * @param dato2 segundo byte de datos (0-127), o 0 si el evento solo tiene uno
 */
public record EventoCanal(long tick, int tipo, int canal, int dato1, int dato2) implements EventoMidi {

    public static final int NOTA_ACABA = 0x80;
    public static final int NOTA_EMPIEZA = 0x90;
    public static final int PRESION_TECLA = 0xA0;
    public static final int CONTROLADOR = 0xB0;
    public static final int INSTRUMENTO = 0xC0;
    public static final int PRESION_CANAL = 0xD0;
    public static final int PITCH_BEND = 0xE0;
}
