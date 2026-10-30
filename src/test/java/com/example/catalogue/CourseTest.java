package com.example.catalogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CourseTest {

    @Test
    void acceptsValidCourse() {
        Course course = new Course("COMP1001", "  Introduction to Programming  ", 4);
        assertEquals("COMP1001", course.code());
        assertEquals("Introduction to Programming", course.title());
        assertEquals(4, course.credits());
    }

    @Test
    void acceptsCodeWithSuffixLetter() {
        assertEquals("CS1010S", new Course("CS1010S", "Programming", 4).code());
    }

    @Test
    void rejectsInvalidCode() {
        assertThrows(IllegalArgumentException.class, () -> new Course("comp1001", "Title", 4));
        assertThrows(IllegalArgumentException.class, () -> new Course("C1001", "Title", 4));
        assertThrows(IllegalArgumentException.class, () -> new Course(null, "Title", 4));
    }

    @Test
    void rejectsBlankOrLongTitle() {
        assertThrows(IllegalArgumentException.class, () -> new Course("COMP1001", " ", 4));
        assertThrows(IllegalArgumentException.class, () -> new Course("COMP1001", "x".repeat(121), 4));
    }

    @Test
    void rejectsCreditsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> new Course("COMP1001", "Title", 0));
        assertThrows(IllegalArgumentException.class, () -> new Course("COMP1001", "Title", 13));
    }

    @Test
    void parsesFromJson() {
        Course course = Course.fromJson("{\"code\":\"MATH2001\",\"title\":\"Linear Algebra\",\"credits\":4}");
        assertEquals(new Course("MATH2001", "Linear Algebra", 4), course);
    }

    @Test
    void rejectsJsonWithWrongTypes() {
        assertThrows(IllegalArgumentException.class,
                () -> Course.fromJson("{\"code\":1,\"title\":\"T\",\"credits\":4}"));
        assertThrows(IllegalArgumentException.class,
                () -> Course.fromJson("{\"code\":\"COMP1001\",\"title\":\"T\",\"credits\":\"4\"}"));
        assertThrows(IllegalArgumentException.class,
                () -> Course.fromJson("{\"code\":\"COMP1001\",\"title\":\"T\",\"credits\":99999999999}"));
    }
}
