package com.example.catalogue;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Thread-safe in-memory store. Data is lost when the application stops. */
public final class CourseRepository {

    private final ConcurrentMap<String, Course> courses = new ConcurrentHashMap<>();

    public static CourseRepository withSampleData() {
        CourseRepository repository = new CourseRepository();
        repository.saveIfAbsent(new Course("COMP1001", "Introduction to Programming", 4));
        repository.saveIfAbsent(new Course("COMP2001", "Data Structures and Algorithms", 4));
        repository.saveIfAbsent(new Course("MATH1001", "Discrete Mathematics", 4));
        repository.saveIfAbsent(new Course("STAT1001", "Probability and Statistics", 3));
        return repository;
    }

    public List<Course> findAll() {
        return courses.values().stream()
                .sorted(Comparator.comparing(Course::code))
                .toList();
    }

    public Optional<Course> findByCode(String code) {
        return Optional.ofNullable(courses.get(code));
    }

    /** Returns true if the course was added, false if a course with the same code already exists. */
    public boolean saveIfAbsent(Course course) {
        return courses.putIfAbsent(course.code(), course) == null;
    }

    public int count() {
        return courses.size();
    }
}
