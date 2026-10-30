package com.example.catalogue;

public class DuplicateCourseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateCourseException(String code) {
        super("Course already exists: " + code);
    }
}
