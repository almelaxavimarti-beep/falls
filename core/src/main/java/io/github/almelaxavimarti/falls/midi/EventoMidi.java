package io.github.almelaxavimarti.falls.midi;

/**
 * Un evento tal como viene en una pista MIDI, con su tick absoluto (desde el principio de la pista).
 * Solo puede ser un evento de canal o un metaevento ("sealed": no hay más tipos posibles).
 */
public sealed interface EventoMidi permits EventoCanal, EventoMeta {

    /** Tick en el que ocurre, en la resolución original del archivo. */
    long tick();
}
