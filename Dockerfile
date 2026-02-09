# Stage 1: build JAR with Maven
FROM maven:3.9-eclipse-temurin-21-alpine AS jar-builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
# Skip docker-maven-plugin when building inside Docker (no Docker daemon available)
RUN mvn -B package -DskipTests -Ddocker.skip=true

# Stage 2: build native binary with GraalVM (glibc-linked)
FROM ghcr.io/graalvm/native-image-community:21 AS native-builder
WORKDIR /build
COPY --from=jar-builder /build/target/injector-*.jar ./app.jar
RUN native-image -jar app.jar -o injector --no-fallback \
    --initialize-at-build-time=org.apache.logging.log4j \
    --initialize-at-build-time=com.fasterxml.jackson \
    -H:+UnlockExperimentalVMOptions \
    -H:+StripDebugInfo \
    -H:+ReportExceptionStackTraces

# Stage 3: Alpine with gcompat so glibc-linked binary runs
FROM alpine:3.19
RUN apk add --no-cache ca-certificates gcompat
COPY --from=native-builder /build/injector /opt/injector
ENTRYPOINT ["/opt/injector"]
