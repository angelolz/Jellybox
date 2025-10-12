FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/Jellybox-*.jar app.jar
ENV DOCKER=true
ENTRYPOINT ["java", "-jar", "app.jar"]
