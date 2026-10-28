package io.github.almelaxavimarti.falls;

/**
 * Punto de entrada del núcleo de Falls.
 * De momento solo sirve para comprobar que el proyecto compila y se prueba.
 */
public final class Falls {

    public static final String NOMBRE = "Falls";

    private Falls() {
    }

    public static String presentacion() {
        return NOMBRE + ": corrector explicativo de melodías y generador de baterías";
    }
}
