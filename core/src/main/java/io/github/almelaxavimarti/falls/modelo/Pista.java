package io.github.almelaxavimarti.falls.modelo;

import java.util.List;
import java.util.Objects;

/** Una pista: nombre, rol, notas, controladores y pitch bends. */
public record Pista(String nombre, Rol rol, List<Nota> notas,
                    List<Controlador> controladores, List<PitchBend> bends) {

    public Pista {
        Objects.requireNonNull(nombre, "El nombre no puede ser null");
        Objects.requireNonNull(rol, "El rol no puede ser null");
        notas = List.copyOf(notas);
        controladores = List.copyOf(controladores);
        bends = List.copyOf(bends);
    }

    /** Constructor corto: una pista solo con notas, sin controladores ni bends. */
    public Pista(String nombre, Rol rol, List<Nota> notas) {
        this(nombre, rol, notas, List.of(), List.of()); // ya está hecho
    }
}
