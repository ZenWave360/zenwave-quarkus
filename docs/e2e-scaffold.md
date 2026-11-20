# E2E archetype scaffold

How the Quarkus projects under `e2e/src/test/resources/projects/` were created, and how to make a new one.

| Project | Model (source of truth) | What e2e runs |
|---|---|---|
| `order-fulfillment-quarkus` | `test-resources/.../zdl/order-fulfillment.zdl` | `-Pzenwave verify -DskipITs=false`: regenerate (hand-edited files kept), all tests incl. `*IT` on Dev Services. `test-compile` only without Docker. |
| `customer-address-relational` | `test-resources/.../zdl/customer-address-relational.zdl` | `-Pzenwave test -Dtest=ArchitectureTest,CustomerCommandsKafkaTest`: fresh generation, compile, architecture rules, Kafka round trip (Docker only). |

Each project keeps a `zenwave-model.zdl` so it runs on its own. The e2e test overwrites it in the `target/projects` copy with the `test-resources` model.

## 1. Base project: `quarkus create`

```bash
mvn io.quarkus.platform:quarkus-maven-plugin:3.40.1:create \
  -DprojectGroupId=io.zenwave360.example \
  -DprojectArtifactId=order-fulfillment-quarkus \
  -DprojectVersion=0.0.1-SNAPSHOT \
  -DnoCode \
  -Dextensions="rest,rest-jackson,hibernate-orm,jdbc-postgresql,hibernate-validator,messaging-kafka,smallrye-openapi,smallrye-health"
```

Kept from the output as is: `mvnw`, `mvnw.cmd`, `.mvn/wrapper`, `.gitignore`, `.dockerignore`, `src/main/docker/*`, BOM import, `quarkus-maven-plugin`, surefire/failsafe config, `native` profile.
The create resolved platform `3.40.1.2`, which the pom pins as `quarkus.platform.version`.

## 2. Changes to the generated pom

| Change | Why |
|---|---|
| `maven.compiler.release` 25 → 21 | LTS floor for users. |
| `quarkus-flyway`, `flyway-database-postgresql` | prod schema migrations. |
| `mapstruct` + `mapstruct-processor` in `annotationProcessorPaths` | `componentModel = cdi` mappers. Not in the Quarkus BOM. |
| `mockito-core`, `testcontainers`, `archunit-junit5` 1.5.1 (test) | generated tests. ArchUnit ≥ 1.5 runs on the JUnit 6 platform of Quarkus 3.40; 1.4.x does not. |
| `openapi-generator-maven-plugin` 7.18.0, `generate-sources` | OpenAPI DTOs only (`spring` generator, models, `DTO` suffix, `Nullable=jakarta.annotation.Nullable`). Controllers come from ZenWave. |
| `zenwave-sdk-maven-plugin`, `generate-sources`, `AsyncAPIGenerator` + `SmallRyeTemplates` | SmallRye producer/consumer, every build, into `target/generated-sources/zenwave`. |
| profile `zenwave`, phase `initialize` | `ZDLToOpenAPI`, `ZDLToAsyncAPI`, `BackendApplicationDefault`, `OpenAPIControllers`, `SpringWebTestClient` (Quarkus templates) into `src/`. Missing files only unless `-Dzenwave.forceOverwrite=true`. |
| `maven-snapshots` repositories | SDK 2.9.0-SNAPSHOT. |
| properties block | the only per-project part: `basePackage`, OpenAPI/AsyncAPI packages (must match the ZDL layout), `databaseType`. |

ZDL options are passed as `configOptions/zdlFile`. The mojo's own `zdlFile`/`zdlFiles` parameters map to the deprecated `zdlFiles` and are not used. The mojo requires `inputSpec`. ZDL-only plugins get the placeholder `${zdl.inputSpec.workaround}`, because a `.zdl` `inputSpec` is turned into a `file:` URI twice by `GeneratorMojo`.

`.mvn/jvm.config` adds `--add-exports jdk.compiler/...` so the palantir formatter runs in the Maven JVM on JDK 17+.

## 3. Files added by hand

| File | Content |
|---|---|
| `zenwave-model.zdl` | copy of the `test-resources` model |
| `src/main/resources/application.properties` | Dev Services in dev/test, env vars in `%prod`, `%compose` profile for `docker-compose.yml`, channel → topic mapping, `mapping.format.global=ignore`, `http.test-port=0`, schema strategy (below) |
| `docker-compose.yml` | Postgres 16 + Kafka (KRaft) for `-Dquarkus.profile=dev,compose` |
| `README.md` | run and generate commands |

Schema strategy (`application.properties`):

- dev/test: Hibernate `drop-and-create` + `src/test/resources/data.sql` via `quarkus.hibernate-orm.sql-load-script`. This is the Quarkus counterpart of Spring Boot `ddl-auto` + `data.sql`. Quarkus has no `schema.sql` equivalent.
- prod: `quarkus.flyway.migrate-at-start=true` + Hibernate `validate`, migrations in `src/main/resources/db/migration`.
- `FlywaySchemaTest` (order-fulfillment) runs the migrations under a test profile and lets Hibernate validate. Entity/migration drift fails the build.

Seed `V1__init.sql` from the Hibernate DDL:

```bash
./mvnw test -Dtest=<AnyQuarkusTest> \
  -Dquarkus.hibernate-orm.scripts.generation=create \
  -Dquarkus.hibernate-orm.scripts.generation.create-target=target/create.sql
```

`customer-address-relational` has no migration: e2e generates it fresh and does not start the prod profile.

## 4. Second project from the first

`customer-address-relational` was copied from `order-fulfillment-quarkus`: wrapper, `.mvn`, docker files, compose, and the pom. Only these were changed: `groupId`/`artifactId`/`name`, the properties block (CleanHexagonal packages under `io.example.orders`), dropping the order-specific `http-tests-business-flow` execution, the hibernate package, the channels and DLQ in `application.properties`, and `producerImplementationPackage`. Generated sources are not checked in. The command adapter and its Kafka test are hand-written.

## 5. New project

1. Copy `customer-address-relational` (fresh) or `order-fulfillment-quarkus` (hand-finished).
2. Edit the properties block to match the ZDL `config` / layout, then `application.properties`: hibernate packages and channels (`grep -rh '@Channel\|@Incoming' target/generated-sources/zenwave` after a first build).
3. Put the model in `test-resources/src/main/resources/zdl/` and add an e2e test with `ScaffoldProject.copy(name, "zdl/<model>.zdl")`.
4. `mvn install -De2e.tests.skip=false` at the repo root. Iterate in place with `e2e/regenerate.sh <name> [--force|--clean]`.
