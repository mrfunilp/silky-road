FROM maven:3.10-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring \
    && mkdir -p /data/images && chown spring:spring /data/images

COPY --from=build /build/target/*.jar app.jar
RUN chown spring:spring /app/app.jar
USER spring

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
