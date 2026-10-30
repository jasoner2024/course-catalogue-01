package com.example.catalogue;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Business logic for the course catalogue. */
public final class CourseService {

    private final CourseRepository repository;

    public CourseService(CourseRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public List<Course> listCourses() {
        return repository.findAll();
    }

    /** Returns courses whose code or title contains the term, ignoring case. Blank term returns all. */
    public List<Course> searchCourses(String term) {
        if (term == null || term.isBlank()) {
            return listCourses();
        }
        String needle = term.trim().toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(c -> c.code().toLowerCase(Locale.ROOT).contains(needle)
                        || c.title().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    /** Looks up a course. The code is matched case-insensitively. */
    public Course getCourse(String code) {
        String normalised = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        return repository.findByCode(normalised)
                .orElseThrow(() -> new CourseNotFoundException(normalised));
    }

    public Course createCourse(Course course) {
        Objects.requireNonNull(course, "course");
        if (!repository.saveIfAbsent(course)) {
            throw new DuplicateCourseException(course.code());
        }
        return course;
    }
}
