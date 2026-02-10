# Project instructions — Person Injector

<!-- Documentation generated from `src/main/documentation/PROJECT_INSTRUCTIONS.md`. Do not edit directly. -->

This document describes what the project does, how to build it, the Docker images it produces, and how to test that everything works (including a minimal Docker image check using the `--console` option).

---

## What the project does

**Person Injector** is a demo data generator for [Elasticsearch](https://www.elastic.co/products/elasticsearch) and [Kibana](https://www.elastic.co/products/kibana). It generates synthetic “person” documents that can be:

- **Indexed into Elasticsearch** (default) — for demos, dashboards, and testing.
- **Printed to the console** — via the `--console` flag, for quick verification or piping.

By default it generates 1,000,000 documents in bulks of 10,000, targeting a local cluster at `https://127.0.0.1:9200` with user `elastic` and password `changeme`. You can change the number of documents (`--nb`), bulk size (`--bulk`), and Elasticsearch connection settings (`--es.host`, `--es.apikey`, `--es.index`, etc.).

The project also ships Kibana saved objects (dashboard, Canvas, visualizations) that can be imported to visualize the generated dataset.

---

## Requirements

- **Java 21** (enforced by the build).
- **Maven** to build and run tests.
- **Docker** (optional) to build and run the container image. GraalVM is not required on the host; the native executable is built inside the Docker build.

---

## How to build

From the project root:

```bash
# Compile, run tests, package the JAR and build the Docker image
mvn clean install
```

To build without running tests:

```bash
mvn clean package -DskipTests
```

**Artifacts:**

- **Flattened JAR:** `target/injector-<version>.jar` (e.g. `target/injector-9.3.1-SNAPSHOT.jar`) — runnable with `java -jar injector-<version>.jar [options]`.
- **Docker image:** Built by the `docker-maven-plugin` during the `package` phase (see below).

The Docker build uses the `Dockerfile` in the project root. It is a multi-stage build: the first stage builds the JAR with Maven from `pom.xml` and `src/`, the second stage uses GraalVM to produce a native executable from that JAR, and the final stage copies only the native binary into a minimal Alpine runtime image. The image is built by the `docker-maven-plugin` when you run `mvn package` (or `mvn install`); you can also run `docker build .` manually from the project root.

---

## Docker images produced

The build generates a single Docker image:

| Image name                  | Tags produced                                          |
|-----------------------------|--------------------------------------------------------|
| `dadoonet/persons-injector` | `<project.version>` (e.g. `9.3.1-SNAPSHOT`) and `latest` |

**Base image:** `alpine:3.23` (minimal image; no JVM). The GraalVM native executable is glibc-linked and runs on Alpine via `gcompat`. The binary is built with `-H:+StripDebugInfo` to reduce size.  
**Entrypoint:** `/opt/injector` — a GraalVM native executable that accepts the same options as the JAR (e.g. `--console`, `--nb`, `--elasticsearch`).

**Examples:**

```bash
# After mvn package / install, run with default Elasticsearch settings
docker run dadoonet/persons-injector:9.3.1-SNAPSHOT

# Or use the 'latest' tag
docker run dadoonet/persons-injector:latest
```

You can override the default command by passing injector options after the image name, e.g. `docker run dadoonet/persons-injector:latest --console --nb 10`.

---

## How to test that the project works

### 1. Unit and integration tests (Maven)

```bash
# Run unit tests only
mvn test

# Run unit tests + integration tests (full test phase)
mvn verify
```

Integration tests (e.g. `*IT.java`) run in the `integration-test` phase and include at least:

- **ConsoleInjectorIT** — runs the injector with `--console --nb 100` to ensure the console path works.
- **ElasticsearchInjectorIT** — tests indexing against an Elasticsearch cluster (e.g. Testcontainers).

So a passing `mvn verify` is the main way to ensure the application and its options (including `--console`) work correctly.

### 2. Quick run of the JAR (console only)

To confirm the app runs and generates data to the console without Elasticsearch:

```bash
mvn clean package -DskipTests
java -jar target/injector-9.3.1-SNAPSHOT.jar --console --nb 10
```

You should see log lines and 10 generated person documents. Add `--cs.pretty` for pretty-printed JSON.

### 3. Minimal check that the Docker image works (recommended: `--console`)

To verify that the **Docker image** is functional without needing a running Elasticsearch, run the container with **`--console`** and a small document count. This checks that the image starts (native binary, no JVM) and the injector prints data to stdout.

```bash
# Build the image (if not already built)
mvn clean package -DskipTests

# Run the image with console output only (no Elasticsearch)
docker run --rm dadoonet/persons-injector:9.3.1-SNAPSHOT --console --nb 5
```

You should see:

- Log output from the injector (e.g. “Generating [5] persons”, “Sample document: …”, “Done injecting [5] persons”).
- The generated person documents printed to stdout.

Optional: use `--cs.pretty` for readable JSON:

```bash
docker run --rm dadoonet/persons-injector:9.3.1-SNAPSHOT --console --nb 5 --cs.pretty
```

If this command completes successfully and prints the expected output, the Docker image is working at a minimum level. You can then test the full Elasticsearch path (e.g. with `--elasticsearch` and a real cluster) as needed.

---

## Summary checklist

| Step                         | Command / action                                                          |
|------------------------------|---------------------------------------------------------------------------|
| Build (with tests)           | `mvn clean install`                                                       |
| Build (no tests)             | `mvn clean package -DskipTests`                                           |
| Run tests only               | `mvn test` or `mvn verify`                                                |
| Run JAR in console mode      | `java -jar target/injector-9.3.1-SNAPSHOT.jar --console --nb 10`            |
| Check Docker image (minimal) | `docker run --rm dadoonet/persons-injector:9.3.1-SNAPSHOT --console --nb 5` |

All of the above should be run from the project root directory. Replace `9.3.1-SNAPSHOT` with your actual project version if different (see `pom.xml` or `mvn help:evaluate -Dexpression=project.version -q -DforceStdout`).
