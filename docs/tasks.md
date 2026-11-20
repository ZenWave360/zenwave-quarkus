# Tasks

Status file for the Quarkus customization. Update the mark on the task that moves. One task is `[/]` at a time.

Marks: `[ ]` not started, `[/]` in progress, `[x]` done / ready for review, `[!]` blocked.

Design: `docs/001-target-stack.md` and `docs/quarkus-customization.md`.

## parent

- [x] Reactor `pom.xml` with dependency management for SDK `2.9.0-SNAPSHOT` and the module list: `test-resources`, `quarkus-backend-application`, `asyncapi-quarkus`, `e2e`.

## test-resources

- [x] Module that publishes `src/main/resources/zdl/`, `src/main/resources/openapi/openapi.yml`, and `src/main/resources/asyncapi/asyncapi.yml` for every other module.

## design

- [x] Target file list for the Quarkus application recorded in `docs/quarkus-customization.md`. Template implementation starts from that list.

## quarkus-backend-application

- [x] Module `pom.xml`. `provided` dependencies on `backend-application-default`, `openapi-controllers`, and `openapi-spring-webtestclient`. Test dependency on `test-resources`.
- [x] Unit test that loads a ZDL from `test-resources` and runs `BackendApplicationDefaultPlugin` with `BackendApplicationQuarkusTemplates`.
- [x] First backend slice from the target list: entity, repository port, `EntityManager` repository implementation, inbound service, service implementation.
- [x] Remaining backend target files from the list, including MapStruct `componentModel = cdi` and the in-process CDI event templates.
- [x] `OpenAPIControllersQuarkusTemplates` and a unit test that emits `{{serviceName}}ApiController.java` from `openapi/openapi.yml`.
- [x] `QuarkusHttpTestTemplates` and a unit test that emits one `@QuarkusTest` class using JDK `HttpClient`.

## asyncapi-quarkus

- [x] `SmallRyeTemplates` selected by the class name on `AsyncAPIGeneratorPlugin`. Same six target files as `SpringKafkaTemplates` for `transactionalOutbox = none`.
- [x] SmallRye Handlebars for the producer and the `@Incoming` consumer, replacing the `kafka/` and `scs/` implementations. Shared producer interface and consumer service interface stay free of SmallRye.
- [x] Unit test that loads `asyncapi/asyncapi.yml` from `test-resources` and asserts `Emitter` and `@Incoming` appear only on the producer and consumer implementations.

## e2e

- [x] Checked-in Quarkus scaffold under `e2e/src/test/resources/projects/`, skipped in the normal reactor build the way [`../zenwave-sdk/e2e`](../zenwave-sdk/e2e) uses `e2e.tests.skip`.
- [x] E2E test copies the scaffold to `target/projects/`, runs the backend, OpenAPI controllers, HTTP tests, and AsyncAPI plugins with the Quarkus templates, then `MavenCompiler.compile` on the copy.

## playground

- [x] `playground/order-fulfillment-quarkus`: `quarkus create` 3.40.1 app, generated via `zenwave-sdk-maven-plugin` (profile `zenwave`), Dev Services tests green (`mvn verify -DskipITs=false`).
- [x] Template fixes found by the playground: natural-id finder impl, `@TestTransaction` repository ITs, business-flow var names, `@Vetoed` in-memory producer, auditing, value-object equality.
- [x] Remove leftover Kotlin templates; `NoKotlinTemplatesTest` guards it.
- [ ] Move the playground into `e2e/src/test/resources/projects/` and migrate `customer-address-relational` to the same scaffold.
- [ ] Fix upstream `GeneratorMojo`: `.zdl` inputSpec double `file:` URI; `zdlFiles` not seen as `zdlFile` by `OpenAPIControllersPlugin`. Drop playground workarounds after.
- [ ] Flyway migrations (prod uses `schema-management.strategy=validate`).
- [ ] JAX-RS exception mappers as generated (or documented) output, now hand-written in the playground.
- [ ] Scaffold quality: `ServiceMapper` comments (`PayOrderInputnull-Order`), `ZDLToOpenAPI` examples ignore `maxLength`, `data.sql` is an LLM prompt, not SQL.
- [ ] ArchUnit 1.4.1 engine fails on the Quarkus 3.40 JUnit platform; e2e scaffold still declares it.
