FROM openjdk:21-jdk-slim-buster

WORKDIR /app

# Configurar variáveis de ambiente
ENV SPRING_PROFILES_ACTIVE=dev \
    JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006"

COPY target/ms-video-admin-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8090 5006

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
