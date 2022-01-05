FROM openjdk:16
RUN mkdir /opt/jukebox
COPY target/Jukebox-1.0.1.jar /opt/jukebox
COPY config.properties .
ENTRYPOINT ["java", "-jar", "/opt/jukebox/Jukebox-1.0.1.jar"]