package com.example.catalogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void usesDefaultPortWhenUnset() {
        assertEquals(App.DEFAULT_PORT, App.parsePort(null));
        assertEquals(App.DEFAULT_PORT, App.parsePort(" "));
    }

    @Test
    void parsesPort() {
        assertEquals(9090, App.parsePort("9090"));
    }

    @Test
    void rejectsInvalidPort() {
        assertThrows(IllegalArgumentException.class, () -> App.parsePort("abc"));
        assertThrows(IllegalArgumentException.class, () -> App.parsePort("70000"));
    }

    @Test
    void prefersVersionFromEnvironment() {
        assertEquals("1.2.3", App.resolveVersion(" 1.2.3 "));
    }

    @Test
    void fallsBackToDevVersionOutsideJar() {
        assertEquals("dev", App.resolveVersion(""));
    }
}
