package com.example.catalogue;

public class CourseNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CourseNotFoundException(String code) {
        super("Course not found: " + code);
    }
}
