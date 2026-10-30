package com.example.catalogue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTP API built on the JDK HTTP server.
 *
 * <pre>
 * GET  /health               {"status":"UP"}
 * GET  /version              {"version":"..."}
 * GET  /api/courses          list all courses
 * GET  /api/courses/{code}   get one course (code is case-insensitive)
 * POST /api/courses          create a course
 * </pre>
 */
public final class ApiServer {

    private static final Logger LOG = Logger.getLogger(ApiServer.class.getName());
    private static final String COURSES_PATH = "/api/courses";
    private static final int MAX_BODY_BYTES = 16 * 1024;

    private final CourseService service;
    private final String version;
    private HttpServer server;

    public ApiServer(CourseService service, String version) {
        this.service = Objects.requireNonNull(service, "service");
        this.version = Objects.requireNonNull(version, "version");
    }

    /** Starts the server. Pass port 0 to use any free port. Returns the port in use. */
    public synchronized int start(int port) throws IOException {
        if (server != null) {
            throw new IllegalStateException("Server is already running");
        }
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", exchange -> handle(exchange, this::health));
        server.createContext("/version", exchange -> handle(exchange, this::version));
        server.createContext(COURSES_PATH, exchange -> handle(exchange, this::courses));
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
        return server.getAddress().getPort();
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    private record Response(int status, String body) {
    }

    @FunctionalInterface
    private interface Route {
        Response apply(HttpExchange exchange) throws IOException;
    }

    private void handle(HttpExchange exchange, Route route) throws IOException {
        Response response;
        try {
            response = route.apply(exchange);
        } catch (CourseNotFoundException e) {
            response = error(404, e.getMessage());
        } catch (DuplicateCourseException e) {
            response = error(409, e.getMessage());
        } catch (IllegalArgumentException e) {
            response = error(400, e.getMessage());
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Unhandled error", e);
            response = error(500, "Internal server error");
        }

        byte[] bytes = response.body().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(response.status(), bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private Response health(HttpExchange exchange) {
        if (!exchange.getRequestURI().getPath().equals("/health")) {
            return error(404, "Not found");
        }
        if (!"GET".equals(exchange.getRequestMethod())) {
            return methodNotAllowed(exchange, "GET");
        }
        return new Response(200, Json.object(Map.of("status", "UP")));
    }

    private Response version(HttpExchange exchange) {
        if (!exchange.getRequestURI().getPath().equals("/version")) {
            return error(404, "Not found");
        }
        if (!"GET".equals(exchange.getRequestMethod())) {
            return methodNotAllowed(exchange, "GET");
        }
        return new Response(200, Json.object(Map.of("version", version)));
    }

    private Response courses(HttpExchange exchange) throws IOException {
        String rest = exchange.getRequestURI().getPath().substring(COURSES_PATH.length());
        String method = exchange.getRequestMethod();

        if (rest.isEmpty() || rest.equals("/")) {
            return switch (method) {
                case "GET" -> new Response(200, Json.toJson(service.listCourses()));
                case "POST" -> {
                    Course created = service.createCourse(Course.fromJson(readBody(exchange)));
                    exchange.getResponseHeaders().set("Location", COURSES_PATH + "/" + created.code());
                    yield new Response(201, Json.toJson(created));
                }
                default -> methodNotAllowed(exchange, "GET, POST");
            };
        }

        if (!rest.startsWith("/")) {
            return error(404, "Not found");
        }
        String code = rest.substring(1);
        if (code.isEmpty() || code.contains("/")) {
            return error(404, "Not found");
        }
        if (!"GET".equals(method)) {
            return methodNotAllowed(exchange, "GET");
        }
        return new Response(200, Json.toJson(service.getCourse(code)));
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            byte[] bytes = in.readNBytes(MAX_BODY_BYTES + 1);
            if (bytes.length > MAX_BODY_BYTES) {
                throw new IllegalArgumentException("Request body is too large");
            }
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private static Response methodNotAllowed(HttpExchange exchange, String allowed) {
        exchange.getResponseHeaders().set("Allow", allowed);
        return error(405, "Method not allowed");
    }

    private static Response error(int status, String message) {
        return new Response(status, Json.object(Map.of("error", message)));
    }
}
