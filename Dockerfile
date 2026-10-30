# Runtime image only. The JAR is built by Maven in CI and copied in from target/.
FROM eclipse-temurin:21-jre

ARG APP_VERSION=dev
ENV APP_VERSION=${APP_VERSION} \
    PORT=8080

RUN useradd --system --uid 10001 --no-create-home appuser

WORKDIR /app
COPY target/course-catalogue.jar /app/app.jar

USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
