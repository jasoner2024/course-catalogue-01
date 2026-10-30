# Course Catalogue API

A small Java 21 REST API for a course catalogue.

The application has no runtime dependencies. It uses the HTTP server built into the JDK.

To set up your laptop, see [GETTING-STARTED.md](GETTING-STARTED.md).

## Endpoints

| Method | Path                  | Description                                  |
|--------|-----------------------|----------------------------------------------|
| GET    | `/health`             | Returns `{"status":"UP"}`                    |
| GET    | `/version`            | Returns `{"version":"..."}` from `APP_VERSION` |
| GET    | `/api/courses`        | Lists all courses                            |
| GET    | `/api/courses/{code}` | Gets one course (code is case-insensitive)   |
| POST   | `/api/courses`        | Creates a course; returns 201 with Location  |

Example request body for POST:

```json
{ "code": "PHYS1001", "title": "Physics I", "credits": 4 }
```

Data is held in memory and is lost when the application stops.

## Configuration

| Variable      | Default                         | Purpose                         |
|---------------|---------------------------------|---------------------------------|
| `PORT`        | `8080`                          | HTTP port                       |
| `APP_VERSION` | JAR manifest version, else `dev` | Value returned by `/version`    |

## Build and run locally

Requirements: JDK 21 and Maven 3.9 or later.

```bash
mvn -B verify                                  # compile, unit tests, integration tests, coverage, JAR
java -jar target/course-catalogue.jar          # start on port 8080
curl http://localhost:8080/health
```

Container image (build the JAR first):

```bash
docker build --build-arg APP_VERSION=local -t course-catalogue:local .
docker run --rm -p 8080:8080 course-catalogue:local
```

## Useful Maven commands

| Command | What it does |
|---------|--------------|
| `mvn -B test` | Unit tests only (`*Test.java`) |
| `mvn -B verify` | Unit tests, integration tests (`*IT.java`), coverage report and JAR |
| `mvn -B verify -DskipUnitTests=true` | Integration tests only, plus JAR |
| `mvn -B help:evaluate -Dexpression=project.version -q -DforceStdout` | Prints the project version |

Report locations:

- Unit test results: `target/surefire-reports/`
- Integration test results: `target/failsafe-reports/`
- Coverage: `target/site/jacoco/` (HTML, `jacoco.xml` and `jacoco.csv`)
- JAR: `target/course-catalogue.jar`

## Project layout

```
.github/workflows/ci.yml    Existing CI pipeline (build, test, image build without push)
src/main/java/...           Application code
src/test/java/...           Unit tests (*Test) and integration tests (*IT)
Dockerfile                  Runtime image; copies the JAR from target/
```

## Existing CI

`ci.yml` runs on pushes and pull requests to `main`. It builds and tests the application,
uploads the test reports and the JAR as artifacts, then builds a container image without
pushing it.
