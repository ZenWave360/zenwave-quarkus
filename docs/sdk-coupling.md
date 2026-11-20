# SDK coupling

This customization is not an official ZenWave SDK module. It plugs into the official plugins through the `templates` option. That option is the only documented extension point. Everything else listed below is SDK implementation detail that can change in any SDK release, snapshots included, without notice.

This document records that coupling: what we depend on, why, and how a break shows up. Use it when bumping `zenwave-sdk.version` and when porting upstream template changes.

## Decision: keep every template local

All templates are copies kept in this repo, including the 22 that are byte-identical to upstream `backend-application-default`, which could be loaded from the SDK jar. A local copy is clearer: one folder holds the whole Quarkus output, and an upstream change never alters the generated code silently. The cost is that upstream template fixes are ported by hand.

Last synced: zenwave-sdk `develop` at `41bbb4aa` (2026-11-20). After a port, update this line and `docs/tasks.md`.

## What we depend on

| SDK surface | Used by | Stability | What a change breaks |
|---|---|---|---|
| `templates` option (`new <class>` or a class name) | all modules | Documented | Template selection |
| `ProjectTemplates`: protected template lists (`aggregateTemplates`, `entityTemplates`, `enumTemplates`, `allDomainEventsTemplates`, `singleTemplates`, …), `addTemplate(…)`, `setTemplatesFolder`, `layout`, `is(model, …)`, `getTemplateHelpers`, `shouldGenerateListeners()` | `BackendApplicationQuarkusTemplates`, `OpenAPIControllersQuarkusTemplates` | Internal | Compile error, or silently missing or extra files when a list is renamed or split |
| `SpringWebTestClientTemplates` overridable methods (`partialTemplate`, `baseTestClassTemplate`, `businessFlowTestTemplate`, `serviceTestTemplate`, `operationTestTemplate`), `SpringWebTestClientGenerator.openApiModelNamePrefix/Suffix` | `QuarkusHttpTestTemplates` | Internal | Compile error; Spring templates generated instead of Quarkus |
| `AbstractAsyncapiGenerator.Templates`: `producerTemplates`, `producerByServiceTemplates`, `consumerByChannelTemplates`, `addTemplate`; `generator.getTemplateEngine()` cast to `HandlebarsEngine`; `AsyncAPIHandlebarsHelpers` | `SmallRyeTemplates` | Internal | Compile error; missing producer or consumer |
| Classpath folder `io/zenwave360/sdk/plugins/AsyncAPIGenerator`: our `smallrye/` templates sit next to the upstream `shared/` interface templates (`IProducer`, `IServiceByChannel`, `EventsProducerInMemoryContext`), which we reuse | `asyncapi-quarkus` | Internal | Missing interface files, or interfaces that no longer match our implementations |
| Unknown options passed through to the template model (`Generator.additionalProperties`) | `producerImplementationPackage` | Internal | The option is ignored and the producer is generated in `producerApiPackage` |
| About 100 upstream Handlebars helpers (`wrapWithMapper`, `isCrudMethod`, `mapperInputCallSignature`, `findAggregateCommandsForMethod`, `aggregateTransitionsClassName`, `runtimeHeadersMap`, `producerInterfaceName`, …). `singleTransitionTo` and `transitionTargets` only exist since 2.9.0-SNAPSHOT | all templates | Internal | Render-time error `could not find helper`, or different output |
| `CustomHandlebarsHelpers.partial`: partials resolve relative to the including template's file | all templates | Internal | Partials not found |
| ZDL model keys: `zdl.allEntitiesAndEnums`, `aggregate.lifecycle`, `method.transition.from/to`, `aggregateCommandsForMethod`, `method.returnTypeIsArray`, `listenerGroup.mapperBindings`, `binding.*`, `operation.adapterPlan.reason`, … | templates, `skip*` functions | Internal | Silently empty output. Handlebars renders a missing key as blank |
| `ZDLJavaSignatureUtils`, `ZDLFindUtils`, `ZDLHttpUtils`, `JSONPath`, `CleanHexagonalProjectLayout`, `ProjectLayout`, `BackendApplicationDefaultGenerator.getIdJavaType()` | `*Helpers.java`, `*Templates.java` | Internal | Compile error, or different Java signatures |
| Option names: `persistence`, `style`, `databaseType`, `includeEmitEventsImplementation`, `implementEventListeners`, `useJMolecules`, `role`, `producerApiPackage`, … | users' `configOptions`, `*Templates` fields | Semi-public (plugin docs) | Option ignored without warning |
| `_runtimeheaders` in the shared producer interface is package-private, so the producer inlines headers with `runtimeHeadersMap` | `smallrye/producer` | Internal | Compile error in generated code |

## How breaks are detected

- **Compile-time:** `mvn install` compiles the `*Templates` and `*Helpers` classes against the SDK jar.
- **Render-time and silent breaks:** the unit tests in each module run the official plugin with our templates on the shared models and assert the generated files (`SmallRyeGeneratorTest`, `EventListenersTest`, …). A missing helper fails here.
- **Behavior:** `mvn install -De2e.tests.skip=false` generates the two e2e projects, then compiles and tests them on Quarkus. Wrong or empty output usually surfaces here, as compile errors or failing `@QuarkusTest`s.
- **Not covered:** a model key that renders blank in a template branch no e2e model exercises. Keep the e2e models broad.

## When bumping the SDK

1. Diff upstream `plugins/backend-application-default`, `openapi-controllers`, `openapi-spring-webtestclient` and `asyncapi-generator` since the last synced commit: Java sources and templates.
2. Check the `ProjectTemplates` / `Templates` classes for renamed or split template lists.
3. Port the template fixes that apply to Java and Quarkus. Record what you skipped, and why, in `docs/tasks.md`.
4. Run the full build with e2e. Update "Last synced" above.

## What a stable contract would look like

These are proposals for the SDK. They would make an external customization depend only on documented API:

- **A public, versioned `Templates` API:** named template slots, for example `slot("entity").replace(location)`, instead of protected lists.
- **Documented model keys and helpers:** a published list per plugin, with deprecation before removal.
- **Strict rendering:** fail on a missing helper or a missing model key in tests, so silent blanks become errors.
- **Public producer contract:** make `_runtimeheaders` public, or expose runtime headers through a helper.
- **Generated provider-side command consumer:** generate the consumer service implementation for provider-role command channels.
