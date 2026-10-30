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

    /** Looks up a course. The code is matched case-insensitively. */
    public Course getCourse(String code) {
        final String normalised = code == null ? "" : code.strip();
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
