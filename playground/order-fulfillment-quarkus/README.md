# Order Fulfillment (Quarkus)

Playground for this repo's Quarkus templates. Same domain as
`zenwave-playground/examples/order-fulfillment-kotlin`, generated as **Java on Quarkus**:
JAX-RS, CDI, Hibernate ORM (`EntityManager` repositories), MapStruct, SmallRye Kafka.

Intended to become an e2e project under `e2e/src/test/resources/projects/`.

## Run

Needs Java 21+, Maven, Docker.

```bash
./mvnw test                       # in-memory + @QuarkusTest (Dev Services: Postgres, Kafka)
./mvnw verify -DskipITs=false     # + *IT business-flow tests
./mvnw quarkus:dev                # Dev Services; Swagger UI at http://localhost:8080/q/swagger-ui

docker compose up -d              # or bring your own infra
./mvnw quarkus:dev -Dquarkus.profile=dev,compose
```

Prod profile reads `DB_URL`, `DB_USER`, `DB_PASSWORD`, `KAFKA_BOOTSTRAP_SERVERS`.

## Generate

Templates come from this repo (`quarkus-backend-application`, `asyncapi-quarkus`): `mvn install` at the repo root first.

| Command | Writes |
|---|---|
| `mvn -Pzenwave generate-sources` | specs, backend, JAX-RS controllers, HTTP tests into `src/` (missing files only) |
| `mvn generate-sources` | every build: OpenAPI DTOs + SmallRye producer/consumer into `target/generated-sources` |
| `../regenerate.sh` | install templates, then `-Pzenwave` (keeps hand edits) |
| `../regenerate.sh --force` | overwrite all generated files in `src/` |
| `../regenerate.sh --clean` | delete generated `src/` code + test data, generate from scratch |

Model: `zenwave-model.zdl`. Plugins and options: `pom.xml`, profile `zenwave`.

## Hand-written (not generated)

- `OrderServiceImpl.placeOrder` body (order number, total, save)
- `WebExceptionMappers`: `NoSuchElementException` → 404, invalid state transition → 409
- tests: `OrderServiceTest`, `OrderApiControllerTest`, `OrderApiIntegrationTest`, `FromPlaceOrderToCancelIT`
- `src/test/resources/data.sql`, `data/jpa/Order/1.json`
- `src/main/resources/application.properties`, `docker-compose.yml`

## Known SDK workarounds (pom.xml)

`zenwave-sdk-maven-plugin` `GeneratorMojo`:

1. `.zdl` as `inputSpec` is wrapped as a `file:` URI twice → pass `zdlFile` + a non-`.zdl` placeholder `inputSpec`.
2. It passes `zdlFiles`; `OpenAPIControllersPlugin` loads the ZDL only when `zdlFile` is set → repeat `zdlFile` in `configOptions`.

JDK 25 + palantir formatter need `jdk.compiler` exports: `.mvn/jvm.config`.
