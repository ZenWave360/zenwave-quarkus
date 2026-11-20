# ZenWave Quarkus

Unofficial [ZenWave SDK](https://github.com/ZenWave360/zenwave-sdk) customization that generates a **Java Quarkus** backend from a ZDL model, plus its OpenAPI and AsyncAPI specs.

It does not ship its own plugins. It is a set of Maven jars of Handlebars templates and helpers. You keep the official ZenWave plugins and select these templates with the `templates` option, the same way the SDK's Kotlin customization is selected.

> Status: `2.9.0-SNAPSHOT`, built against ZenWave SDK `2.9.0-SNAPSHOT`. Snapshots are published to the ZenWave snapshots repository on every push to `main`. Not on Maven Central yet.

Add the snapshots repository to your build. You need it as a plugin repository too, because `zenwave-sdk-maven-plugin` is a snapshot:

```xml
<repositories>
    <repository>
        <id>zenwave-snapshots</id>
        <releases><enabled>false</enabled></releases>
        <snapshots><enabled>true</enabled></snapshots>
        <url>https://www.zenwave360.io/maven-snapshots/</url>
    </repository>
</repositories>
<pluginRepositories>
    <pluginRepository>
        <id>zenwave-snapshots</id>
        <releases><enabled>false</enabled></releases>
        <snapshots><enabled>true</enabled></snapshots>
        <url>https://www.zenwave360.io/maven-snapshots/</url>
    </pluginRepository>
</pluginRepositories>
```

## What it generates

| ZenWave plugin | `templates` option | Output |
|---|---|---|
| `BackendApplicationDefaultPlugin` | `new io.zenwave360.sdk.plugins.quarkus.BackendApplicationQuarkusTemplates` | Domain entities and aggregates, application services, ports, JPA repositories (`EntityManager`), MapStruct mappers, CDI domain events and listeners, auditing, tests (ArchUnit, in-memory, repository ITs) |
| `OpenAPIControllersPlugin` | `new io.zenwave360.sdk.plugins.quarkus.OpenAPIControllersQuarkusTemplates` | Jakarta REST (JAX-RS) controllers and DTO mappers |
| `SpringWebTestClientPlugin` | `new io.zenwave360.sdk.plugins.quarkus.QuarkusHttpTestTemplates` | `@QuarkusTest` HTTP tests with the JDK `HttpClient`, per operation and per business flow |
| `AsyncAPIGenerator` | `io.zenwave360.sdk.plugins.quarkus.SmallRyeTemplates` | SmallRye Reactive Messaging (Kafka) producers and consumers behind framework-free interfaces |

The generated code is:

- **Imperative Java.** No `Uni`, `Multi`, `Mono` or `Flux` in business code.
- **Hexagonal.** The core does not depend on messaging or HTTP, and ArchUnit enforces it.
- **Plain CDI.** It wires with `@ApplicationScoped` and constructor injection.
- **Transactional at the application boundary,** with `jakarta.transaction.Transactional`.

## Modules

| Module | Artifact | Role |
|---|---|---|
| `quarkus-backend-application` | `io.zenwave360.sdk:quarkus-backend-application` | Backend, JAX-RS controllers and HTTP test templates |
| `asyncapi-quarkus` | `io.zenwave360.sdk:asyncapi-quarkus` | SmallRye producer and consumer templates |
| `test-resources` | (not published) | Shared ZDL, OpenAPI and AsyncAPI models for the tests |
| `e2e` | (not published) | Generates and builds two real Quarkus projects |

## Usage

Add the customization jar as a dependency of `zenwave-sdk-maven-plugin`, next to the official plugin it customizes, and set `templates`.

Backend:

```xml
<plugin>
    <groupId>io.zenwave360.sdk</groupId>
    <artifactId>zenwave-sdk-maven-plugin</artifactId>
    <version>${zenwave.version}</version>
    <executions>
        <execution>
            <id>backend-application</id>
            <phase>initialize</phase>
            <goals><goal>generate</goal></goals>
            <configuration>
                <generatorName>BackendApplicationDefaultPlugin</generatorName>
                <inputSpec>zenwave-model.zdl</inputSpec>
                <targetFolder>${project.basedir}</targetFolder>
                <configOptions>
                    <templates>new io.zenwave360.sdk.plugins.quarkus.BackendApplicationQuarkusTemplates</templates>
                    <basePackage>io.example.orders</basePackage>
                    <persistence>jpa</persistence>
                    <databaseType>postgresql</databaseType>
                    <style>imperative</style>
                    <includeEmitEventsImplementation>true</includeEmitEventsImplementation>
                    <!-- <implementEventListeners>true</implementEventListeners> for @listener bindings -->
                </configOptions>
            </configuration>
        </execution>
    </executions>
    <dependencies>
        <dependency>
            <groupId>io.zenwave360.sdk.plugins</groupId>
            <artifactId>backend-application-default</artifactId>
            <version>${zenwave.version}</version>
        </dependency>
        <dependency>
            <groupId>io.zenwave360.sdk</groupId>
            <artifactId>quarkus-backend-application</artifactId>
            <version>${zenwave-quarkus.version}</version>
        </dependency>
    </dependencies>
</plugin>
```

Controllers and HTTP tests use the same dependency with `OpenAPIControllersPlugin` / `SpringWebTestClientPlugin`. Add `openapi-controllers` and `openapi-spring-webtestclient` as plugin dependencies.

AsyncAPI (SmallRye):

```xml
<configuration>
    <generatorName>AsyncAPIGenerator</generatorName>
    <inputSpec>src/main/resources/public/apis/asyncapi.yml</inputSpec>
    <configOptions>
        <role>provider</role>
        <templates>io.zenwave360.sdk.plugins.quarkus.SmallRyeTemplates</templates>
        <modelPackage>io.example.orders.core.outbound.events.dtos</modelPackage>
        <producerApiPackage>io.example.orders.core.outbound.events</producerApiPackage>
        <consumerApiPackage>io.example.orders.adapters.commands</consumerApiPackage>
        <!-- optional: put the SmallRye producer implementation outside the port package -->
        <producerImplementationPackage>io.example.orders.infrastructure.events</producerImplementationPackage>
    </configOptions>
</configuration>
<!-- plugin dependencies: io.zenwave360.sdk.plugins:asyncapi-generator, io.zenwave360.sdk:asyncapi-quarkus -->
```

The full, working configuration is in [`e2e/src/test/resources/projects/order-fulfillment-quarkus/pom.xml`](e2e/src/test/resources/projects/order-fulfillment-quarkus/pom.xml), in the `zenwave` profile. The templates only write sources into an existing Quarkus project. They do not create the `pom.xml`, the main class or the Docker Compose file. To set up a project, see [docs/e2e-scaffold.md](docs/e2e-scaffold.md).

## Runtime semantics

| Concern | Behavior |
|---|---|
| Domain events | `EventPublisher` port, implemented with CDI `Event`. |
| Internal listeners (`@listener`) | `@Observes(during = AFTER_SUCCESS)` + `@Transactional(REQUIRES_NEW)`. Same contract as Spring Modulith `@ApplicationModuleListener`, but synchronous: runs after the publisher commits, never on rollback. At-most-once. |
| Kafka producer | `Emitter<String>`, JSON payload, `__TypeId__` header. Inside a JTA transaction it sends after commit; outside one it waits for the broker ack. At-most-once, no outbox yet. |
| Kafka consumer | `@Incoming` + `@Blocking`, typed by `__TypeId__`. On failure it nacks the message, and `failure-strategy=dead-letter-queue` sends it to `dead-letter-topic-<channel>`. |
| Auditing | JPA entity listener with an injected `AuditorProvider`. Falls back to `"system"`. |

## What you still write by hand

Generated code marks the business decisions it cannot derive from the model with `// TODO CUSTOM_REQUIRED`:

- **`@transition` with several `to` states:** choose the target state.
- **Transition commands without an id:** load the entity or aggregate the transition applies to, for example by a correlation key.
- **Listeners:** resolve the entity id when the event carries none; map the event to the service input for custom bindings.
- **AsyncAPI consumer service:** an adapter the SDK could not plan (it gives the reason).

Plain `// TODO` comments mark optional work, such as search by criteria or unimplemented custom service methods. For a provider-side command channel, the consumer service implementation is also hand-written. See `CustomerCommandsService` in the customer-address-relational e2e project.

## Build

```bash
mvn install                          # templates + unit tests
mvn install -De2e.tests.skip=false   # + generate and build the e2e projects
```

The e2e projects run Kafka and the database through Quarkus Dev Services (Testcontainers), so they need Docker. Without Docker, `customer-address-relational` runs only its ArchUnit test.

| e2e project | Covers |
|---|---|
| [`order-fulfillment-quarkus`](e2e/src/test/resources/projects/order-fulfillment-quarkus) | Aggregates, lifecycle transitions, auditing, Flyway, REST, business-flow IT |
| [`customer-address-relational`](e2e/src/test/resources/projects/customer-address-relational) | Relationships, Kafka command consumer and event producer, DLQ |

## SDK coupling

The `templates` option is the only documented extension point. This project also depends on SDK internals: template lists, helper overriding by name, upstream Handlebars helpers, ZDL model keys and utility classes. Any of these can change between SDK snapshots.

All templates are kept as local copies, even the ones identical to upstream, so the output is easy to read in one place. Upstream fixes are ported by hand. Last synced: zenwave-sdk `develop` at `41bbb4aa` (2026-11-20).

The full list, how breaks are detected, and the SDK bump checklist are in [docs/sdk-coupling.md](docs/sdk-coupling.md).

## Docs

- [docs/001-target-stack.md](docs/001-target-stack.md): stack, modules, constraints.
- [docs/quarkus-customization.md](docs/quarkus-customization.md): target files and Quarkus substitutions.
- [docs/e2e-scaffold.md](docs/e2e-scaffold.md): creating a project that uses the templates.
- [docs/sdk-coupling.md](docs/sdk-coupling.md): SDK internals this depends on, and the bump checklist.
- [docs/tasks.md](docs/tasks.md): status and open items.

## CI

- **Push to `main`:** builds, runs the unit tests, and publishes the snapshot jars with [git-maven-publisher](https://github.com/ivangsa/git-maven-publisher), through the shared `ZenWave360/release-workflows` workflow. `test-resources` and `e2e` are not published.
- **Pull requests:** build and unit tests only.
- **e2e:** runs locally only, with `-De2e.tests.skip=false`.

## License

[MIT](LICENSE)
