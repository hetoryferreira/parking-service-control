# ===== Stage 1: build =====
FROM gradle:8.9-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:21-jre
ENV TZ=UTC \
    JAVA_OPTS="-Xms256m -Xmx512m"
WORKDIR /app

# copia o jar gerado (qualquer nome em build/libs/*.jar)
COPY --from=build /workspace/build/libs/*.jar /app/app.jar

EXPOSE 3003
ENTRYPOINT ["bash","-lc","java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev}"]
