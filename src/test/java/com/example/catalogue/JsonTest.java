package com.example.catalogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonTest {

    @Test
    void serialisesCourse() {
        assertEquals("{\"code\":\"COMP1001\",\"title\":\"Intro\",\"credits\":4}",
                Json.toJson(new Course("COMP1001", "Intro", 4)));
    }

    @Test
    void serialisesEmptyList() {
        assertEquals("[]", Json.toJson(List.of()));
    }

    @Test
    void escapesSpecialCharacters() {
        assertEquals("\"a\\\"b\\\\c\\nd\\u0001\"", Json.quote("a\"b\\c\nd\u0001"));
    }

    @Test
    void parsesObjectWithAllValueTypes() {
        Map<String, Object> map = Json.parseObject(
                " { \"s\" : \"x\\ty\\u0041\", \"n\": -12, \"t\": true, \"f\": false, \"z\": null } ");
        assertEquals("x\tyA", map.get("s"));
        assertEquals(-12L, map.get("n"));
        assertEquals(Boolean.TRUE, map.get("t"));
        assertEquals(Boolean.FALSE, map.get("f"));
        assertTrue(map.containsKey("z"));
    }

    @Test
    void parsesEmptyObject() {
        assertTrue(Json.parseObject("{}").isEmpty());
    }

    @Test
    void rejectsInvalidJson() {
        assertThrows(IllegalArgumentException.class, () -> Json.parseObject(""));
        assertThrows(IllegalArgumentException.class, () -> Json.parseObject("[1,2]"));
        assertThrows(IllegalArgumentException.class, () -> Json.parseObject("{\"a\":1"));
        assertThrows(IllegalArgumentException.class, () -> Json.parseObject("{\"a\":1.5}"));
        assertThrows(IllegalArgumentException.class, () -> Json.parseObject("{\"a\":1} extra"));
        assertThrows(IllegalArgumentException.class, () -> Json.parseObject("{\"a\":\"\\q\"}"));
        assertThrows(IllegalArgumentException.class, () -> Json.parseObject("{\"a\" 1}"));
    }
}
