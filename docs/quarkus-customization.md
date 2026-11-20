# Quarkus customization

Quarkus template and helper decisions. The shared customization pattern, the modules, and the stack are in `docs/001-target-stack.md`.

This repository is an example of customizing ZenWave outside the upstream SDK. The project shape to copy is the Kotlin customization:

- [`../zenwave-sdk/plugins/customizations/kotlin-backend-application`](../zenwave-sdk/plugins/customizations/kotlin-backend-application)
- [ZenWave360/zenwave-sdk `plugins/customizations/kotlin-backend-application`](https://github.com/ZenWave360/zenwave-sdk/tree/main/plugins/customizations/kotlin-backend-application)

Read plugin classes, generator options, and the processed model from `../zenwave-sdk`.

The target files below are the implementation contract. Template work starts from this list.

## How templates are selected

`quarkus-backend-application` defines no `Plugin`. Callers set `templates` on the official plugin:

```java
new BackendApplicationDefaultPlugin()
    .withZdlFile("classpath:zdl/orders.zdl")
    .withTargetFolder("target/orders")
    .withOption("templates", "new io.zenwave360.sdk.plugins.quarkus.BackendApplicationQuarkusTemplates")
    .withOption("basePackage", "io.example.orders")
    .withOption("persistence", PersistenceType.jpa)
    .withOption("style", ProgrammingStyle.imperative);
```

| Official plugin | Templates class | Option value |
|---|---|---|
| `BackendApplicationDefaultPlugin` | `BackendApplicationQuarkusTemplates` | `new io.zenwave360.sdk.plugins.quarkus.BackendApplicationQuarkusTemplates` |
| `OpenAPIControllersPlugin` | `OpenAPIControllersQuarkusTemplates` | `new io.zenwave360.sdk.plugins.quarkus.OpenAPIControllersQuarkusTemplates` |
| `SpringWebTestClientPlugin` | `QuarkusHttpTestTemplates` | `new io.zenwave360.sdk.plugins.quarkus.QuarkusHttpTestTemplates` |
| `AsyncAPIGeneratorPlugin` | `SmallRyeTemplates` | `io.zenwave360.sdk.plugins.quarkus.SmallRyeTemplates` |

The AsyncAPI value is a class name. `AsyncAPIGenerator` loads it with `Class.forName` and calls the `AsyncAPIGenerator` constructor, the same path as a custom name beside `SpringCloudStream` and `SpringKafka`.

`BackendApplicationQuarkusTemplates` and `OpenAPIControllersQuarkusTemplates` extend `ProjectTemplates`. `QuarkusHttpTestTemplates` extends `SpringWebTestClientTemplates` and overrides each template method, as `SpringWebTestClientKotlinTemplates` does. `SmallRyeTemplates` extends `AbstractAsyncapiGenerator.Templates`, as `SpringKafkaTemplates` does.

`BackendApplicationDefaultGenerator.onPropertiesSet` still registers `BackendApplicationDefaultHelpers` and `BackendApplicationDefaultJpaHelpers`. Quarkus partials do not use those helpers for annotations, transactions, repositories, or paging.

Package root for the backend jar: `io.zenwave360.sdk.plugins.quarkus`. Handlebars for that jar live under `src/main/resources/io/zenwave360/sdk/plugins/quarkus/<OfficialGeneratorSimpleName>/`.

## Backend target files

Imperative JPA only. Paths sit under the layout package the official templates already use. `<base>` is `src/main/java` or `src/test/java` plus that package. Generated sources are Java.

| Target file | When |
|---|---|
| `<base>/{{aggregate.name}}.java` | each aggregate |
| `<base>/{{aggregateTransitionsClassName}}.java` | each aggregate |
| `<base>/{{event.name}}.java` | each domain event |
| `<base>/{{entity.name}}.java` | each JPA entity |
| `<base>/{{entityServiceTransitionsClassName}}.java` | each entity with a lifecycle |
| `<base>/{{entity.className}}Repository.java` | each entity, outbound port |
| `<base>/{{entity.className}}RepositoryImpl.java` | each entity, infrastructure `EntityManager` bean |
| `<base>/{{enum.name}}.java` | each domain, input, and event enum |
| `<base>/{{inputOrOutput.className}}.java` | each input and output |
| `<base>/{{service.name}}.java` | each inbound service |
| `<base>/{{service.name}}Impl.java` | each service implementation |
| `<base>/{{service.name}}Mapper.java` | each service |
| `<base>/BaseMapper.java` | once |
| `<base>/EventsMapper.java` | core events mapper, once |
| `<base>/{{service.name}}Test.java` | each service |
| `<base>/{{listenerGroup.className}}.java` | event listeners |
| `<base>/{{listenerGroup.className}}Mapper.java` | listener mapper |
| `<base>/EventsMapper.java` | AsyncAPI adapter mapper, generated once |
| `<base>/{{consumerServiceName}}.java` | AsyncAPI consumer adapter shell, generated once |
| `<base>/EventPublisher.java` | events included |
| `<base>/DefaultEventPublisher.java` | events included |
| `<base>/InMemoryEventPublisher.java` | events included |
| `<base>/RepositoriesInMemoryConfig.java` | once |
| `<base>/ServicesInMemoryConfig.java` | once |
| `<base>/InMemoryJpaRepository.java` | once |
| `<base>/{{entity.className}}RepositoryInMemory.java` | each entity |
| `<base>/Auditable.java`, `AuditorProvider.java`, `AuditingEntityListener.java` | once, any `@auditing` entity (entities common package) |
| `<base>/BaseRepositoryIntegrationTest.java` | once, `@QuarkusTest` + `@TestTransaction` |
| `<base>/{{entity.className}}RepositoryIntegrationTest.java` | each entity |
| `<base>/TestDataLoader.java` | once |
| `<base>/DockerComposeInitializer.java` | once |
| `<base>/package-info.java` | inbound DTOs, and infrastructure |
| `<base>/ArchitectureTest.java` | clean hexagonal layout |
| `<base>/JMoleculesArchitectureTest.java` | `useJMolecules` |
| `src/test/resources/data/jpa/{{entity.name}}/1.json` | each entity |
| `src/test/resources/data.sql` | JPA, when the official template would emit it |

`{{entity.className}}RepositoryImpl.java` is the Quarkus addition. The official Java templates and the Kotlin customization extend Spring Data and do not emit a repository implementation.

These templates are not registered: MongoDB entities, repositories, fixtures, and Docker initializer; reactive repositories; Modulith metadata.

## OpenAPI controller target files

`OpenAPIControllersGenerator` sets `webFlavor` to `mvc` or `webflux`. The Quarkus templates class registers `web/jaxrs/ServiceApiController.java` and does not use `web/{{webFlavor}}`.

| Target file | When |
|---|---|
| `<base>/PatchMap.java` | the OpenAPI model contains PATCH |
| `<base>/BaseMapper.java` | once, web mappers |
| `<base>/{{serviceName}}DTOsMapper.java` | each service |
| `<base>/{{serviceName}}ApiController.java` | each service |
| `<base>/{{serviceName}}ApiControllerTest.java` | `includeControllerTests` |

## HTTP test target files

`QuarkusHttpTestTemplates` overrides the same entry points as `SpringWebTestClientKotlinTemplates`.

| Target file | When |
|---|---|
| `<base>/BaseQuarkusHttpTest.java` | once |
| `<base>/BusinessFlowTest.java` | business-flow grouping |
| `<base>/{{serviceName}}IT.java` | each service |
| `<base>/{{serviceName}}/{{operationId}}IT.java` | each operation |
| `<base>/Operation.java` | operation partial written as a source, same slot as the Kotlin partial |

## AsyncAPI SmallRye target files

`asyncapi-quarkus` registers the same six outputs as `SpringKafkaTemplates` when `transactionalOutbox` is `none`. Output stays Java. `SmallRyeTemplates` uses the classpath folder `io/zenwave360/sdk/plugins/AsyncAPIGenerator`, so the shared producer interface, the payload-and-headers consumer interface, `EventsProducerInMemoryContext`, and the header partials load from the `asyncapi-generator` jar. The three replacements are `smallrye/producer/Producer.java`, `smallrye/producer/InMemoryEventsProducer.java`, and `smallrye/consumer/<style>/Consumer.java`. A Modulith outbox producer is not registered.

| Target file | Source |
|---|---|
| `src/main/java/{{producerApiPackage}}/{{producerInterfaceName}}.java` | shared `IProducer.java.hbs`, no SmallRye |
| `src/main/java/{{producerApiPackage}}/{{producerClassName}}.java` | SmallRye hbs, replaces `kafka/producer` and `scs/producer` |
| `src/test/java/{{producerApiPackage}}/{{producerInMemoryName}}.java` | CDI in-memory producer |
| `src/test/java/{{producerApiPackage}}/EventsProducerInMemoryContext.java` | CDI test context, replaces the Spring context |
| `src/main/java/{{consumerApiPackage}}/{{consumerServiceInterfaceName}}.java` | shared payload-and-headers interface |
| `src/main/java/{{consumerApiPackage}}/{{consumerName}}.java` | SmallRye hbs, `@Incoming`, replaces `kafka/consumer` and `scs/consumer` |

Header fragments stay includes. `Headers.hbs` stays on the shared producer interface. The shared `wrapNullPayload.hbs` (Spring `KafkaNull` via `Class.forName`) is not used: a `null` payload is sent as a Kafka tombstone. `runtime-headers.hbs` is a CDI copy under `smallrye/producer/partials/` because the shared partial evaluates expressions with Spring SpEL and `ApplicationContext`. A `$message.payload#/...` expression is a JSON pointer over `objectMapper.valueToTree(payload)` (no bean reflection). A `$supplier` name is a `@Named` `Supplier` bean from an injected `@Any Instance<Object>`. `#{...}` SpEL is logged and skipped. The producer passes the runtime-headers map inline because `Headers._runtimeheaders` is package-private in the port. The payload-and-headers consumer interface is the shared template. The `exposeMessage` branch imports `org.springframework.messaging.Message` and is not part of this set. The Modulith transactional-outbox producer is not part of this set.

Extra option `producerImplementationPackage` (pass-through): package of the SmallRye producer class, default `producerApiPackage`. Hexagonal layouts set it to `<base>.infrastructure.events`, so `core` holds only the port. `ArchitectureTest` forbids SmallRye, Kafka and JAX-RS types in `core`.

`{{consumerServiceName}}.java` in the backend list is the developer-owned adapter, in the same slot as the Kotlin customization. It implements the generated consumer port and delegates to the application service. The SmallRye `@Incoming` class is `{{consumerName}}.java` from this module.

## Quarkus substitutions

Application service implementation: `@ApplicationScoped` and `jakarta.transaction.Transactional`. The constructor takes the outbound repository ports, the MapStruct mapper, and, when that service emits events, the AsyncAPI events producer or the in-process `EventPublisher` and the events mapper. Method bodies live in JPA partials and call the repository port. `ServicesInMemoryConfig` builds the same constructor with `Mappers.getMapper` and `InMemoryEventPublisher`.

Inbound service: a plain Java interface.

Lifecycle (`@transition`): a single `to` target is set after the update. Several targets render a `TODO CUSTOM_REQUIRED` naming them (SDK helpers `singleTransitionTo` / `transitionTargets`).

Entity: Jakarta Persistence `@Entity` in the domain package, same placement as the official JPA entity template. Auditing (`@auditing`): Hibernate `@CreationTimestamp` / `@UpdateTimestamp` for dates; `@EntityListeners(AuditingEntityListener.class)` and `implements Auditable` for `createdBy` / `lastModifiedBy`. The listener (a CDI bean in Quarkus) injects `Instance<AuditorProvider>`, so ArC keeps an application-supplied provider (application-supplied, e.g. from `SecurityIdentity`) and falls back to `system`. No `CDI.current()`, no reflection, so native builds need no registration. Types without an id (`@json`, `@embedded`) get value `equals` / `hashCode`. Getters and setters are explicit. Lombok stays off.

Repository port: finder and save operations the service partials call. Repository implementation: `@ApplicationScoped`, `EntityManager`.

Mappers: `@Mapper(componentModel = "cdi")`, injected.

`EventPublisher` is a framework-free port. `DefaultEventPublisher` fires `jakarta.enterprise.event.Event`. Listeners (`@listener`, `implementEventListeners`) are `@ApplicationScoped` and delegate to the application service. Each method is `@Observes(during = TransactionPhase.AFTER_SUCCESS)` + `@Transactional(REQUIRES_NEW)`: the CDI counterpart of Spring Modulith `@ApplicationModuleListener`, without async. It runs after the publisher commits, never on rollback, immediately when no transaction is active, in its own transaction. A failure does not undo the publisher's commit, so delivery is at-most-once. Verified on Narayana with a `@QuarkusTest` probe.

JAX-RS resource: `@ApplicationScoped`, `@Path`, verb annotations, constructor injection of the inbound service and the DTO mapper. It does not implement a generated Spring interface. The return type is the response body, or `jakarta.ws.rs.core.Response` when the operation needs an explicit status or location. A paginated operation calls the service with `int page, int size` and maps the `List` into the paginated DTO by setting `content`. `PatchMap.java` uses the same skip rule as `OpenAPIControllersKotlinTemplates`.

HTTP tests: `@QuarkusTest`, `@TestHTTPResource` as a `URI`, `@TestHTTPEndpoint` when the generated resource class is known, and `java.net.http.HttpClient`. The base class is `BaseQuarkusHttpTest` and holds the client. Service and operation tests set `@TestHTTPEndpoint` to the generated `*ApiController`. One class per REST resource. One test method per OpenAPI operation. The templates emit Java.

SmallRye producer: `@ApplicationScoped`, constructor-injected `Emitter<String>` per `@Channel`, Jackson `ObjectMapper` and `Instance<TransactionSynchronizationRegistry>`, implementing the shared producer interface. `@Channel` and `@Incoming` use the AsyncAPI channel name. Payloads are JSON text (Quarkus picks `StringSerializer`/`StringDeserializer`, no serializer config) with a `__TypeId__` header holding the payload class name, the Spring Kafka `JsonSerializer` convention.

- Inside an active JTA transaction the message is registered with `registerInterposedSynchronization` and sent in `afterCompletion` only on `STATUS_COMMITTED`. A rollback drops it. The call never blocks the transaction or its DB connection. Delivery is at-most-once (failures logged). A transactional outbox is the at-least-once path.
- Outside a transaction it sends and waits for the broker ack (`app.kafka.topics.<channel>.timeout`, default 10 s), then returns the shared `boolean`.
- `kafka_messageKey` becomes the record key through `OutgoingKafkaRecordMetadata`. Other headers become Kafka headers.

SmallRye consumer: `@Incoming` + `@Blocking` (worker thread, because the service is `@Transactional` JPA), taking `Message<String>`. The type comes from the `__TypeId__` header (fully qualified or simple name, so producers in other packages match) or is the channel's only message type. Unknown types become a Jackson tree for `defaultHandler`. It dispatches with `instanceof` to the shared consumer service interface and acks, or nacks on any exception, bad JSON included. Set `mp.messaging.incoming.<channel>.failure-strategy=dead-letter-queue` so a poison message goes to `dead-letter-topic-<channel>` instead of stopping the channel. The record key is stored as `kafka_receivedMessageKey`. Producer and consumer carry `@RegisterForReflection` for the message types (native Jackson).

In-memory producer: a plain `@Vetoed` class with a public no-arg constructor. `EventsProducerInMemoryContext` constructs it with `new`, so it never competes with the SmallRye producer bean in `@QuarkusTest`. It stores payload and headers in `CapturedMessage` (concurrent map and lists) and keeps `getCapturedMessages`. Avro payloads are out of scope for the JSON text path. Schema Registry stays connector configuration.

Business and application code stays imperative. Method signatures do not use Mutiny (`Uni`, `Multi`), Reactor (`Mono`, `Flux`), Spring Data (`Page`, `Pageable`), or `Optional`. A paginated operation takes `int page, int size` and returns `List`. A method the model marks optional returns the value or null. `findById` returns the entity or null. A natural-id finder on the repository port returns `Optional`, and the service calls `.orElse(null)`.

## Tests

Unit tests in `quarkus-backend-application` and `asyncapi-quarkus` load models from `test-resources`, run `MainGenerator`, and assert the target files and the Quarkus types in them.

`e2e` copies its Quarkus scaffold, writes OpenAPI and AsyncAPI from `classpath:zdl/customer-address-relational.zdl`, runs the four plugins with the templates options above, and compiles the copy. The spec generators use `idType` `integer` and `idTypeFormat` `int64`, so path ids are `Long` and the AsyncAPI operations are `on` plus the event name under the `Customer` tag. `AsyncAPIGeneratorPlugin` writes Java into `io.example.orders.core.outbound.events` and `io.example.orders.core.outbound.events.dtos`, the packages `CleanHexagonalProjectLayout` gives that base package. The controller templates reference OpenAPI models and do not emit them. The scaffold runs `openapi-generator-maven-plugin` 7.18.0 (generator `spring`, models only, suffix `DTO`, `useSpringBoot3`, `documentationProvider` `none`) when `openapi.skip` is false. Optional fields are imported as `jakarta.annotation.Nullable`. Those models are Java beans, so MapStruct can map `Address` to `AddressDTO`. MapStruct will not map a `java.util.List` onto a bean, so the paginated mapper method sets `content` from the list mapping. The scaffold pom imports Quarkus BOM `3.40.1` and compiles Java sources with the MapStruct 1.6.3 processor on the compiler classpath. Testcontainers 2.0.5 comes from that BOM. Generated sources stay on `src/main/java` and `src/test/java`. OpenAPI models are Java under `target/generated-sources/openapi/src/main/java`.
