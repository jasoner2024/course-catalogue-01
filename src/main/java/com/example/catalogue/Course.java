package com.example.catalogue;

import java.util.Map;
import java.util.regex.Pattern;

/** A course in the catalogue. Instances are always valid. */
public record Course(String code, String title, int credits) {

    private static final Pattern CODE_PATTERN = Pattern.compile("[A-Z]{2,4}[0-9]{4}[A-Z]?");
    static final int MIN_CREDITS = 1;
    static final int MAX_CREDITS = 12;
    static final int MAX_TITLE_LENGTH = 120;

    public Course {
        code = code == null ? null : code.strip();
        if (code == null || !CODE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException("Invalid course code: " + code);
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Course title is required");
        }
        title = title.trim();
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("Course title must be at most " + MAX_TITLE_LENGTH + " characters");
        }
        if (credits < MIN_CREDITS || credits > MAX_CREDITS) {
            throw new IllegalArgumentException(
                    "Credits must be between " + MIN_CREDITS + " and " + MAX_CREDITS + ": " + credits);
        }
    }

    /** Builds a course from a JSON object such as {"code":"COMP1001","title":"...","credits":4}. */
    public static Course fromJson(String body) {
        Map<String, Object> fields = Json.parseObject(body);

        if (!(fields.get("code") instanceof String codeValue)) {
            throw new IllegalArgumentException("Field 'code' must be a string");
        }
        if (!(fields.get("title") instanceof String titleValue)) {
            throw new IllegalArgumentException("Field 'title' must be a string");
        }
        if (!(fields.get("credits") instanceof Long creditsValue)) {
            throw new IllegalArgumentException("Field 'credits' must be a whole number");
        }
        if (creditsValue < Integer.MIN_VALUE || creditsValue > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Field 'credits' is out of range");
        }
        return new Course(codeValue, titleValue, creditsValue.intValue());
    }
}
