package com.example.catalogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Starts the real HTTP server on a free port and calls it over HTTP. */
class CourseApiIT {

    private ApiServer server;
    private HttpClient client;
    private String baseUrl;

    @BeforeEach
    void startServer() throws IOException {
        server = new ApiServer(new CourseService(CourseRepository.withSampleData()), "test-version");
        int port = server.start(0);
        baseUrl = "http://localhost:" + port;
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .method(method, publisher)
                .header("Content-Type", "application/json")
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void healthReturnsUp() throws Exception {
        HttpResponse<String> response = send("GET", "/health", null);
        assertEquals(200, response.statusCode());
        assertEquals("{\"status\":\"UP\"}", response.body());
    }

    @Test
    void versionReturnsConfiguredVersion() throws Exception {
        HttpResponse<String> response = send("GET", "/version", null);
        assertEquals(200, response.statusCode());
        assertEquals("{\"version\":\"test-version\"}", response.body());
    }

    @Test
    void listsSampleCourses() throws Exception {
        HttpResponse<String> response = send("GET", "/api/courses", null);
        assertEquals(200, response.statusCode());
        assertTrue(response.body().startsWith("[{\"code\":\"COMP1001\""));
    }

    @Test
    void getsCourseByCodeIgnoringCase() throws Exception {
        HttpResponse<String> response = send("GET", "/api/courses/stat1001", null);
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"credits\":3"));
    }

    @Test
    void returns404ForUnknownCourseOrPath() throws Exception {
        assertEquals(404, send("GET", "/api/courses/XYZ9999", null).statusCode());
        assertEquals(404, send("GET", "/api/courses/COMP1001/extra", null).statusCode());
        assertEquals(404, send("GET", "/healthz", null).statusCode());
    }

    @Test
    void createsCourse() throws Exception {
        HttpResponse<String> created = send("POST", "/api/courses",
                "{\"code\":\"PHYS1001\",\"title\":\"Physics I\",\"credits\":4}");
        assertEquals(201, created.statusCode());
        assertEquals("/api/courses/PHYS1001", created.headers().firstValue("Location").orElse(""));
        assertEquals(200, send("GET", "/api/courses/PHYS1001", null).statusCode());
    }

    @Test
    void rejectsDuplicateCourse() throws Exception {
        HttpResponse<String> response = send("POST", "/api/courses",
                "{\"code\":\"COMP1001\",\"title\":\"Duplicate\",\"credits\":4}");
        assertEquals(409, response.statusCode());
    }

    @Test
    void rejectsInvalidInput() throws Exception {
        assertEquals(400, send("POST", "/api/courses", "not json").statusCode());
        assertEquals(400, send("POST", "/api/courses",
                "{\"code\":\"PHYS1001\",\"title\":\"Physics I\",\"credits\":0}").statusCode());
    }

    @Test
    void rejectsUnsupportedMethod() throws Exception {
        HttpResponse<String> response = send("DELETE", "/api/courses/COMP1001", null);
        assertEquals(405, response.statusCode());
        assertEquals("GET", response.headers().firstValue("Allow").orElse(""));
    }
}
