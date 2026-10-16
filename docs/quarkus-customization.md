# Quarkus customization

Quarkus template and helper decisions. The shared customization pattern, the modules, and the stack are in `docs/001-target-stack.md`.

The pattern to mirror is the Kotlin customization:

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

Imperative JPA only. Paths sit under the layout package the Kotlin templates already use. `<base>` is `src/main/kotlin` or `src/test/kotlin` plus that package.

| Target file | When |
|---|---|
| `<base>/{{aggregate.name}}.kt` | each aggregate |
| `<base>/{{aggregateTransitionsClassName}}.kt` | each aggregate |
| `<base>/{{event.name}}.kt` | each domain event |
| `<base>/{{entity.name}}.kt` | each JPA entity |
| `<base>/{{entityServiceTransitionsClassName}}.kt` | each entity with a lifecycle |
| `<base>/{{entity.className}}Repository.kt` | each entity, outbound port |
| `<base>/{{entity.className}}RepositoryImpl.kt` | each entity, infrastructure `EntityManager` bean |
| `<base>/{{enum.name}}.kt` | each domain, input, and event enum |
| `<base>/{{inputOrOutput.className}}.kt` | each input and output |
| `<base>/{{service.name}}.kt` | each inbound service |
| `<base>/{{service.name}}Impl.kt` | each service implementation |
| `<base>/{{service.name}}Mapper.kt` | each service |
| `<base>/BaseMapper.kt` | once |
| `<base>/EventsMapper.kt` | core events mapper, once |
| `<base>/{{service.name}}Test.kt` | each service |
| `<base>/{{listenerGroup.className}}.kt` | event listeners |
| `<base>/{{listenerGroup.className}}Mapper.kt` | listener mapper |
| `<base>/EventsMapper.kt` | AsyncAPI adapter mapper, generated once |
| `<base>/{{consumerServiceName}}.kt` | AsyncAPI consumer adapter shell, generated once |
| `<base>/EventPublisher.kt` | events included |
| `<base>/DefaultEventPublisher.kt` | events included |
| `<base>/InMemoryEventPublisher.kt` | events included |
| `<base>/RepositoriesInMemoryConfig.kt` | once |
| `<base>/ServicesInMemoryConfig.kt` | once |
| `<base>/InMemoryJpaRepository.kt` | once |
| `<base>/{{entity.className}}RepositoryInMemory.kt` | each entity |
| `<base>/BaseRepositoryIntegrationTest.kt` | once |
| `<base>/{{entity.className}}RepositoryIntegrationTest.kt` | each entity |
| `<base>/TestDataLoader.kt` | once |
| `<base>/DockerComposeInitializer.kt` | once |
| `<base>/package-info.kt` | inbound DTOs, and infrastructure |
| `<base>/ArchitectureTest.kt` | clean hexagonal layout |
| `<base>/JMoleculesArchitectureTest.kt` | `useJMolecules` |
| `src/test/resources/data/jpa/{{entity.name}}/1.json` | each entity |
| `src/test/resources/data.sql` | JPA, when the Kotlin template would emit it |

`{{entity.className}}RepositoryImpl.kt` is the Quarkus addition. The Kotlin templates extend Spring Data and do not emit a repository implementation.

These Kotlin templates are not registered: MongoDB entities, repositories, fixtures, and Docker initializer; reactive repositories; `ModuleMetadata.kt` and the common Modulith metadata.

## OpenAPI controller target files

`OpenAPIControllersGenerator` sets `webFlavor` to `mvc` or `webflux`. The Quarkus templates class registers `web/jaxrs/ServiceApiController.kt` and does not use `web/{{webFlavor}}`.

| Target file | When |
|---|---|
| `<base>/PatchMap.kt` | the OpenAPI model contains PATCH |
| `<base>/BaseMapper.kt` | once, web mappers |
| `<base>/{{serviceName}}DTOsMapper.kt` | each service |
| `<base>/{{serviceName}}ApiController.kt` | each service |
| `<base>/{{serviceName}}ApiControllerTest.kt` | `includeControllerTests` |

## HTTP test target files

`QuarkusHttpTestTemplates` overrides the same entry points as `SpringWebTestClientKotlinTemplates`.

| Target file | When |
|---|---|
| `<base>/BaseQuarkusHttpTest.kt` | once |
| `<base>/BusinessFlowTest.kt` | business-flow grouping |
| `<base>/{{serviceName}}IT.kt` | each service |
| `<base>/{{serviceName}}/{{operationId}}IT.kt` | each operation |
| `<base>/Operation.kt` | operation partial written as a source, same slot as the Kotlin partial |

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

Header fragments stay includes. `Headers.hbs` stays on the shared producer interface. `wrapNullPayload.hbs` is the shared partial. `runtime-headers.hbs` is a CDI copy under `smallrye/producer/partials/` because the shared partial evaluates expressions with Spring SpEL and `ApplicationContext`. A `$message.payload#/...` expression is read with `PropertyDescriptor`. A `$supplier` name is resolved with `CDI.current()` and `@Named`. `#{...}` SpEL is logged and skipped. The payload-and-headers consumer interface is the shared template. The `exposeMessage` branch imports `org.springframework.messaging.Message` and is not part of this set. The Modulith transactional-outbox producer is not part of this set.

`{{consumerServiceName}}.kt` in the backend list is the developer-owned adapter from the Kotlin customization. It implements the generated consumer port and delegates to the application service. The SmallRye `@Incoming` class is `{{consumerName}}.java` from this module.

## Quarkus substitutions

Application service implementation: `@ApplicationScoped` and `jakarta.transaction.Transactional`. The constructor takes the outbound repository ports, the MapStruct mapper, and, when that service emits events, the AsyncAPI events producer or the in-process `EventPublisher` and the events mapper. Method bodies live in JPA partials and call the repository port. `ServicesInMemoryConfig` builds the same constructor with `Mappers.getMapper` and `InMemoryEventPublisher`.

Inbound service: a plain Kotlin interface.

Entity: Jakarta Persistence `@Entity` in the domain package, same placement as the Kotlin JPA entity template. Auditing annotations, when the model asks for them, are Jakarta Persistence or Hibernate annotations.

Repository port: finder and save operations the service partials call. Repository implementation: `@ApplicationScoped`, `EntityManager`.

Mappers: `@Mapper(componentModel = "cdi")`, injected.

`EventPublisher` is a framework-free port. `DefaultEventPublisher` fires `jakarta.enterprise.event.Event`. Listeners are `@ApplicationScoped` with `@Observes` and delegate to the application service.

JAX-RS resource: `@ApplicationScoped`, `@Path`, verb annotations, constructor injection of the inbound service and the DTO mapper. It does not implement a generated Spring interface. The return type is the response body, or `jakarta.ws.rs.core.Response` when the operation needs an explicit status or location. A paginated operation calls the service with `page` and `size` and maps the `List` into the paginated DTO. `PatchMap.kt` uses the same skip rule as `OpenAPIControllersKotlinTemplates`.

HTTP tests: `@QuarkusTest`, `@TestHTTPResource` as a `URI`, `@TestHTTPEndpoint` when the generated resource class is known, and `java.net.http.HttpClient`. The base class is `BaseQuarkusHttpTest` and holds the client. Service and operation tests set `@TestHTTPEndpoint` to the generated `*ApiController`. One class per REST resource. One test method per OpenAPI operation. `SpringWebTestClientPlugin` does not run `KotlinFormatter`, so the templates emit valid Kotlin.

SmallRye producer: `@ApplicationScoped`, constructor-injected `Emitter` and `@Channel`, implementing the shared producer interface. `@Channel` and `@Incoming` use the AsyncAPI channel name. The producer sends a `Message` and waits on that message's acknowledgement, because `Emitter.send(Message)` returns void, then returns the shared `boolean`. `kafka_messageKey` becomes the Kafka record key through `OutgoingKafkaRecordMetadata`. Other headers become Kafka headers. SmallRye consumer: `@Incoming` inside `{{consumerName}}.java`, taking a MicroProfile `Message`, dispatching with `instanceof` to the shared consumer service interface, and acknowledging that message. The record key is stored as `kafka_receivedMessageKey`, which the shared headers getter already reads. The in-memory producer is `@ApplicationScoped` with a public no-arg constructor, so `EventsProducerInMemoryContext` can still construct it with `new`. It stores payload and headers in `CapturedMessage` and keeps `getCapturedMessages`. Avro payloads are the Java type the model already declares. Schema Registry stays connector configuration.

Business and application code stays imperative. Method signatures do not use Mutiny (`Uni`, `Multi`), Reactor (`Mono`, `Flux`), or Spring Data (`Page`, `Pageable`). A paginated operation takes `page: Int` and `size: Int` and returns `List`.

## Tests

Unit tests in `quarkus-backend-application` and `asyncapi-quarkus` load models from `test-resources`, run `MainGenerator`, and assert the target files and the Quarkus types in them.

`e2e` copies its Quarkus scaffold, writes OpenAPI and AsyncAPI from `classpath:zdl/customer-address-relational.zdl`, runs the four plugins with the templates options above, and compiles the copy. The spec generators use `idType` `integer` and `idTypeFormat` `int64`, so path ids are `Long` and the AsyncAPI operations are `on` plus the event name under the `Customer` tag. `AsyncAPIGeneratorPlugin` writes Java into `io.example.orders.core.outbound.events` and `io.example.orders.core.outbound.events.dtos`, the packages `CleanHexagonalProjectLayout` gives that base package. The controller templates reference OpenAPI models and do not emit them. The scaffold runs `openapi-generator-maven-plugin` 7.18.0 (generator `spring`, models only, suffix `DTO`, `useSpringBoot3`, `documentationProvider` `none`) when `openapi.skip` is false. Optional fields are imported as `jakarta.annotation.Nullable`. Those models are Java beans, so MapStruct can map `Address` to `AddressDTO`. MapStruct will not map a `java.util.List` onto a bean, so the paginated mapper method sets `content` from the list mapping. `kapt` runs in `compile`, after `javac`, so MapStruct sees the AsyncAPI Java types and the OpenAPI beans. The scaffold pom imports Quarkus BOM `3.40.1`, configures `kotlin-maven-plugin` 2.4.10 and the MapStruct 1.6.3 processor, and compiles the generated Java AsyncAPI sources together with the Kotlin backend. Testcontainers 2.0.5 comes from that BOM. Java compilation stays on `src/main/java` and `src/test/java`. Kotlin compilation stays on `src/main/kotlin` and `src/test/kotlin`. OpenAPI models are Java under `target/generated-sources/openapi/src/main/java`.
