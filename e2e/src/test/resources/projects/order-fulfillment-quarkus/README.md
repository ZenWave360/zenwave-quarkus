# Order Fulfillment (Quarkus)

E2E project for this repo's Quarkus templates. Same domain as
`zenwave-playground/examples/order-fulfillment-kotlin`, generated as **Java on Quarkus**:
JAX-RS, CDI, Hibernate ORM (`EntityManager` repositories), MapStruct, SmallRye Kafka, Flyway.

Scaffold origin and conventions: [`docs/e2e-scaffold.md`](../../../../../../docs/e2e-scaffold.md).

## Run

Needs Java 21+, Maven, Docker. Install the templates first: `mvn install` at the repo root.

```bash
./mvnw test                       # in-memory + @QuarkusTest (Dev Services: Postgres, Kafka) + FlywaySchemaTest
./mvnw verify -DskipITs=false     # + *IT business-flow tests
./mvnw quarkus:dev                # Dev Services; Swagger UI at http://localhost:8080/q/swagger-ui

docker compose up -d              # or bring your own infra
./mvnw quarkus:dev -Dquarkus.profile=dev,compose
```

Prod profile: Flyway `migrate-at-start`, Hibernate `validate`. Reads `DB_URL`, `DB_USER`, `DB_PASSWORD`, `KAFKA_BOOTSTRAP_SERVERS`.

## Generate

| Command | Writes |
|---|---|
| `mvn -Pzenwave generate-sources` | specs, backend, JAX-RS controllers, HTTP tests into `src/` (missing files only) |
| `mvn generate-sources` | every build: OpenAPI DTOs + SmallRye producer/consumer into `target/generated-sources` |
| `e2e/regenerate.sh` | install templates, then `-Pzenwave` (keeps hand edits) |
| `e2e/regenerate.sh --force` | overwrite all generated files in `src/` |
| `e2e/regenerate.sh --clean` | delete generated `src/` code + test data, generate from scratch |

Model: `zenwave-model.zdl`, a copy of `test-resources/src/main/resources/zdl/order-fulfillment.zdl` (e2e uses that one).

## Hand-written (not generated)

- `OrderServiceImpl.placeOrder` body (order number, total, save)
- `WebExceptionMappers`: `NoSuchElementException` → 404, invalid state transition → 409
- tests: `AuditingTest` + `config/TestAuditorProvider`, `OrderServiceTest`, `OrderApiControllerTest`, `OrderApiIntegrationTest`, `FromPlaceOrderToCancelIT`, `FlywaySchemaTest`
- `src/main/resources/db/migration/V1__init.sql`
- `src/test/resources/data.sql`, `data/jpa/Order/1.json`
- `src/main/resources/application.properties`, `docker-compose.yml`
