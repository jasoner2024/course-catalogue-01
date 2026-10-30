package com.example.catalogue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Minimal JSON support for flat objects. This keeps the application free of runtime
 * dependencies.
 */
public final class Json {

    private Json() {
    }

    public static String toJson(Course course) {
        return "{\"code\":" + quote(course.code())
                + ",\"title\":" + quote(course.title())
                + ",\"credits\":" + course.credits() + "}";
    }

    public static String toJson(List<Course> courses) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        courses.forEach(course -> joiner.add(toJson(course)));
        return joiner.toString();
    }

    /** Serialises a map of string fields as a JSON object. */
    public static String object(Map<String, String> fields) {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        fields.forEach((key, value) -> joiner.add(quote(key) + ":" + quote(value)));
        return joiner.toString();
    }

    public static String quote(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 2).append('"');
        for (char ch : value.toCharArray()) {
            switch (ch) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }

    /** Parses a flat JSON object whose values are strings, whole numbers, booleans or null. */
    public static Map<String, Object> parseObject(String text) {
        Parser parser = new Parser(text);
        Map<String, Object> result = parser.readObject();
        parser.skipWhitespace();
        if (!parser.atEnd()) {
            throw parser.error("Unexpected content after JSON object");
        }
        return result;
    }

    private static final class Parser {

        private final String text;
        private int pos;

        Parser(String text) {
            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException("Request body is empty");
            }
            this.text = text;
        }

        boolean atEnd() {
            return pos >= text.length();
        }

        void skipWhitespace() {
            while (!atEnd() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }

        char peek() {
            if (atEnd()) {
                throw error("Unexpected end of input");
            }
            return text.charAt(pos);
        }

        void expect(char expected) {
            skipWhitespace();
            if (peek() != expected) {
                throw error("Expected '" + expected + "'");
            }
            pos++;
        }

        IllegalArgumentException error(String message) {
            return new IllegalArgumentException("Invalid JSON at position " + pos + ": " + message);
        }

        Map<String, Object> readObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            expect('{');
            skipWhitespace();
            if (peek() == '}') {
                pos++;
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = readString();
                expect(':');
                skipWhitespace();
                map.put(key, readValue());
                skipWhitespace();
                char next = peek();
                if (next == '}') {
                    pos++;
                    return map;
                }
                if (next != ',') {
                    throw error("Expected ',' or '}'");
                }
                pos++;
            }
        }

        Object readValue() {
            char ch = peek();
            if (ch == '"') {
                return readString();
            }
            if (ch == '-' || Character.isDigit(ch)) {
                return readWholeNumber();
            }
            if (text.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            }
            if (text.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            }
            if (text.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw error("Unsupported value");
        }

        Long readWholeNumber() {
            int start = pos;
            if (peek() == '-') {
                pos++;
            }
            while (!atEnd() && Character.isDigit(text.charAt(pos))) {
                pos++;
            }
            if (!atEnd() && ".eE".indexOf(text.charAt(pos)) >= 0) {
                throw error("Only whole numbers are supported");
            }
            String number = text.substring(start, pos);
            if (number.equals("-")) {
                throw error("Invalid number");
            }
            try {
                return Long.parseLong(number);
            } catch (NumberFormatException e) {
                throw error("Number out of range");
            }
        }

        String readString() {
            if (peek() != '"') {
                throw error("Expected a string");
            }
            pos++;
            StringBuilder sb = new StringBuilder();
            while (true) {
                char ch = peek();
                pos++;
                if (ch == '"') {
                    return sb.toString();
                }
                if (ch != '\\') {
                    sb.append(ch);
                    continue;
                }
                char escape = peek();
                pos++;
                switch (escape) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (pos + 4 > text.length()) {
                            throw error("Incomplete unicode escape");
                        }
                        try {
                            sb.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                        } catch (NumberFormatException e) {
                            throw error("Invalid unicode escape");
                        }
                        pos += 4;
                    }
                    default -> throw error("Invalid escape character");
                }
            }
        }
    }
}
