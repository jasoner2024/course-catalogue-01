package com.example.catalogue;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Application entry point.
 *
 * <p>Environment variables:
 * <ul>
 *   <li>PORT: HTTP port to listen on (default 8080)</li>
 *   <li>APP_VERSION: version string returned by /version (default: JAR manifest version, else "dev")</li>
 * </ul>
 */
public final class App {

    private static final Logger LOG = Logger.getLogger(App.class.getName());
    static final int DEFAULT_PORT = 8080;

    private App() {
    }

    public static void main(String[] args) throws IOException {
        int port = parsePort(System.getenv("PORT"));
        String version = resolveVersion(System.getenv("APP_VERSION"));

        CourseService service = new CourseService(CourseRepository.withSampleData());
        ApiServer server = new ApiServer(service, version);
        int actualPort = server.start(port);

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "shutdown"));
        LOG.info(() -> "Course Catalogue API version " + version + " listening on port " + actualPort);
    }

    static int parsePort(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_PORT;
        }
        try {
            int port = Integer.parseInt(value.trim());
            if (port < 0 || port > 65535) {
                throw new IllegalArgumentException("PORT must be between 0 and 65535: " + value);
            }
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("PORT must be a number: " + value, e);
        }
    }

    static String resolveVersion(String envValue) {
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        String manifestVersion = App.class.getPackage().getImplementationVersion();
        return manifestVersion != null ? manifestVersion : "dev";
    }
}
