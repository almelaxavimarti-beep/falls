package io.github.almelaxavimarti.falls.modelo;

import java.util.List;

/** Una pista: nombre, rol, notas, controladores y pitch bends. */
public record Pista(String nombre, Rol rol, List<Nota> notas,
                    List<Controlador> controladores, List<PitchBend> bends) {

    public Pista {
        // TODO 1: rechazar nombre o rol nulos.             Pista: Objects.requireNonNull
        // TODO 2: guardar copias que no se puedan modificar de las tres listas.
        //         Pista: notas = List.copyOf(notas);  (y lo mismo con las otras dos)
        //         Esto además rechaza listas nulas.
    }

    /** Constructor corto: una pista solo con notas, sin controladores ni bends. */
    public Pista(String nombre, Rol rol, List<Nota> notas) {
        this(nombre, rol, notas, List.of(), List.of()); // ya está hecho
    }
}
