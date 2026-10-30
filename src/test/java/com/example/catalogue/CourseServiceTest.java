package com.example.catalogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CourseServiceTest {

    private CourseService service;

    @BeforeEach
    void setUp() {
        service = new CourseService(CourseRepository.withSampleData());
    }

    @Test
    void listsCoursesSortedByCode() {
        List<String> codes = service.listCourses().stream().map(Course::code).toList();
        assertEquals(List.of("COMP1001", "COMP2001", "MATH1001", "STAT1001"), codes);
    }

    @Test
    void getsCourseIgnoringCase() {
        assertEquals("Discrete Mathematics", service.getCourse(" math1001 ").title());
    }

    @Test
    void throwsWhenCourseMissing() {
        assertThrows(CourseNotFoundException.class, () -> service.getCourse("XYZ9999"));
        assertThrows(CourseNotFoundException.class, () -> service.getCourse(null));
    }

    @Test
    void createsNewCourse() {
        Course course = new Course("PHYS1001", "Physics I", 4);
        assertEquals(course, service.createCourse(course));
        assertEquals(5, service.listCourses().size());
    }

    @Test
    void rejectsDuplicateCourse() {
        Course duplicate = new Course("COMP1001", "Another Title", 2);
        assertThrows(DuplicateCourseException.class, () -> service.createCourse(duplicate));
        assertEquals("Introduction to Programming", service.getCourse("COMP1001").title());
    }
}
