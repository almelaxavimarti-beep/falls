package io.github.almelaxavimarti.falls;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FallsTest {

    @Test
    void laPresentacionEmpiezaPorElNombre() {
        assertTrue(Falls.presentacion().startsWith("Falls"));
    }
}
