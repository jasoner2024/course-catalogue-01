package com.example.catalogue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class CourseSearchTest {

    private final CourseService service = new CourseService(CourseRepository.withSampleData());

    private List<String> codes(String term) {
        return service.searchCourses(term).stream().map(Course::code).toList();
    }

    @Test
    void blankTermReturnsAllCourses() {
        assertEquals(4, codes(null).size());
        assertEquals(4, codes("  ").size());
    }

    @Test
    void matchesCodeOrTitleIgnoringCase() {
        assertEquals(List.of("COMP1001", "COMP2001"), codes("comp"));
        assertEquals(List.of("STAT1001"), codes("STATISTICS"));
    }

    @Test
    void returnsEmptyListWhenNothingMatches() {
        assertEquals(List.of(), codes("chemistry"));
    }
}
