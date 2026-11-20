# 001 Target Stack

## General purpose

This repository is an example of a ZenWave backend customization that lives outside the upstream SDK. A customization copies the project shape of the Kotlin customization:

- [`../zenwave-sdk/plugins/customizations/kotlin-backend-application`](../zenwave-sdk/plugins/customizations/kotlin-backend-application)
- [ZenWave360/zenwave-sdk `plugins/customizations/kotlin-backend-application`](https://github.com/ZenWave360/zenwave-sdk/tree/main/plugins/customizations/kotlin-backend-application)

Build a Maven jar of Handlebars templates and helpers. The jar does not define its own plugin. Callers keep the official plugin and select the customization with the `templates` option, the same way the Kotlin module is selected with `new io.zenwave360.sdk.plugins.kotlin.BackendApplicationKotlinTemplates`.

Dependencies follow that module. They are `provided`, and the version is `${project.version}` from the SDK customization parent (`2.9.0-SNAPSHOT`). The Kotlin module depends on:

- `io.zenwave360.sdk.plugins:backend-application-default`
- `io.zenwave360.sdk.plugins:openapi-controllers`
- `io.zenwave360.sdk.plugins:openapi-spring-webtestclient`

A later customization depends on the official plugins whose templates it replaces.

The generator project uses Java 17 or later, Maven, JUnit 5, and the SDK `HandlebarsEngine`.

Future customizations copy this section and add their own target section.

## Modules

| Module | Role |
|---|---|
| parent | Reactor `pom.xml`, dependency management, module list. |
| `test-resources` | Shared models for every other module: ZDL, `openapi.yml`, and `asyncapi.yml`. |
| `quarkus-backend-application` | Java customization: backend, OpenAPI controllers, and HTTP tests. |
| `asyncapi-quarkus` | SmallRye templates for `AsyncAPIGenerator`. The Kotlin customization has no equivalent. |
| `e2e` | Checked-in Quarkus project. Same shape as [`../zenwave-sdk/e2e`](../zenwave-sdk/e2e). |

`test-resources` puts the models on the classpath:

```
src/main/resources/zdl/
src/main/resources/openapi/openapi.yml
src/main/resources/asyncapi/asyncapi.yml
```

Customization modules and `e2e` depend on it. They do not keep their own copies of those models.

## Quarkus

The generated application uses:

- Quarkus, imperative.
- Java for the backend, the OpenAPI controllers, the HTTP tests, and the AsyncAPI producer and consumer. The templates classes are `BackendApplicationQuarkusTemplates`, `OpenAPIControllersQuarkusTemplates`, and `QuarkusHttpTestTemplates`, selected on `BackendApplicationDefaultPlugin`, `OpenAPIControllersPlugin`, and `SpringWebTestClientPlugin`. `asyncapi-quarkus` replaces the producer and consumer implementations from `SpringKafkaTemplates` and `SpringCloudStreamTemplates`.
- Maven.
- CDI for wiring.
- Jakarta REST (JAX-RS) for controllers.
- Hibernate ORM with plain JPA repositories: a repository interface and an `@ApplicationScoped` implementation that uses `EntityManager`. Panache active record stays out of the domain.
- `@Transactional` (`jakarta.transaction`) at the application boundary.
- MapStruct for DTO and entity mapping, `componentModel = cdi`. The e2e scaffold pom wires the MapStruct annotation processor into the Java compile.
- CDI events (`Event` / `@Observes`) for in-process domain events, in the same template slots as the Kotlin customization's `EventPublisher` and listener templates.
- SmallRye Reactive Messaging for Kafka inside the AsyncAPI producer and consumer implementations. `Emitter`, `@Channel`, and `@Incoming` stay in those two classes.
- Avro plus Schema Registry where the AsyncAPI model declares them, on that same adapter.

The target file list, template classes, and Quarkus substitutions are in `docs/quarkus-customization.md`. That list is the contract for implementation.

### AsyncAPI templates

`AsyncAPIGenerator.templates` accepts `SpringCloudStream`, `SpringKafka`, or a class name. `asyncapi-quarkus` supplies that class:

```java
new AsyncAPIGeneratorPlugin()
    .withOption("templates", "io.zenwave360.sdk.plugins.quarkus.SmallRyeTemplates");
```

`SmallRyeTemplates` takes an `AsyncAPIGenerator` constructor argument, as `SpringKafkaTemplates` does. It registers the same target files as `SpringKafkaTemplates` for `transactionalOutbox = none`. The producer interface and the consumer service interface stay the shared templates. The producer and consumer implementations are SmallRye Handlebars in place of `kafka/` and `scs/`.

### Hard constraints on generated code

- No reactive types in business or application layers. No `Uni`, no `Multi`, no `Mono`, no `Flux`.
- Business producer interfaces and application services do not import SmallRye. `Emitter`, `@Channel`, and `@Incoming` live only in the AsyncAPI producer and consumer implementations.
- Listeners delegate to a plain Java application service. The application service they call does not import the messaging framework.
- Persistence is hidden behind a repository interface. The JPA implementation uses `EntityManager`.
- Generated HTTP tests use Quarkus test infrastructure (`@QuarkusTest`, `@TestHTTPEndpoint`, `@TestHTTPResource`) plus JDK `HttpClient`. REST Assured is absent from the e2e scaffold.

### Version pinning policy

Pin Quarkus, MapStruct, SmallRye, and Testcontainers versions in the e2e scaffold pom through the Quarkus BOM. The templates do not emit the BOM import or otherwise own project dependency management. Record the chosen Quarkus version in that scaffold.

## Testing

Unit tests live in the customization module that owns the templates. A unit test loads a model from `test-resources`, runs the official plugin with the Quarkus `templates` option, and asserts the generated files.

`e2e` follows [`../zenwave-sdk/e2e`](../zenwave-sdk/e2e). A test copies a checked-in Quarkus scaffold from `e2e/src/test/resources/projects/` to `target/projects/`, runs the plugins in stages against the shared models, and compiles the copy with `MavenCompiler.compile`. E2E tests are skipped in a normal reactor build and run when the e2e property is turned on, the same way `e2e.tests.skip` works in the SDK.

Templates write sources into that existing project. They do not emit `pom.xml`, the Quarkus main class, the Maven wrapper, Docker Compose, or a README. Those files belong to the e2e scaffold.
