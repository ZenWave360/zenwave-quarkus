package io.zenwave360.sdk.plugins.quarkus;

import io.zenwave360.sdk.generators.Generator;
import io.zenwave360.sdk.options.PersistenceType;
import io.zenwave360.sdk.options.ProgrammingStyle;
import io.zenwave360.sdk.plugins.BackendApplicationDefaultGenerator;
import io.zenwave360.sdk.templating.TemplateInput;
import io.zenwave360.sdk.utils.JSONPath;
import io.zenwave360.sdk.zdl.ProjectTemplates;
import io.zenwave360.sdk.zdl.layouts.CleanHexagonalProjectLayout;
import io.zenwave360.sdk.zdl.layouts.ProjectLayout;
import io.zenwave360.sdk.zdl.utils.ZDLFindUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.zenwave360.sdk.templating.OutputFormatType.JSON;
import static io.zenwave360.sdk.templating.OutputFormatType.JAVA;
import static io.zenwave360.sdk.templating.OutputFormatType.SQL;
import static io.zenwave360.sdk.zdl.utils.ZDLFindUtils.is;

/**
 * Quarkus templates for {@code BackendApplicationDefaultPlugin}, selected with the {@code templates} option.
 *
 * <p>Registrations follow
 * {@code ../zenwave-sdk/plugins/customizations/kotlin-backend-application}
 * ({@code BackendApplicationKotlinTemplates}). Generated sources are Java.
 * MongoDB, reactive, and Modulith templates are not registered.
 */
public class BackendApplicationQuarkusTemplates extends ProjectTemplates {

    /** Bound from the flat 'useJMolecules' option, the same one ZDLProcessor reads. */
    public boolean useJMolecules = false;

    public PersistenceType persistence = PersistenceType.jpa;

    public ProgrammingStyle style = ProgrammingStyle.imperative;

    public boolean includeEmitEventsImplementation = true;

    public boolean implementEventListeners = false;

    @Override
    public boolean shouldGenerateListeners() {
        return implementEventListeners;
    }

    protected Function<Map<String, Object>, Boolean> skipEntity = (model) -> is(model, "vo", "input");

    protected Function<Map<String, Object>, Boolean> skipEntityRepository = (model) -> is(model, "persistence")
            || !(is(model, "aggregate") || is(model, "lifecycle")
                    || ZDLFindUtils.isAggregateRoot(JSONPath.get(model, "zdl"), JSONPath.get(model, "$.entity.name")));

    protected Function<Map<String, Object>, Boolean> skipNonAggregateTestData = (model) ->
            is(model, "embedded")
                    || !(is(model, "aggregate") || is(model, "lifecycle")
                    || ZDLFindUtils.isAggregateRoot(JSONPath.get(model, "zdl"), JSONPath.get(model, "$.entity.name")));

    protected Function<Map<String, Object>, Boolean> skipAggregateTransitions = (model) -> {
        var aggregate = (Map<String, Object>) model.get("aggregate");
        if (aggregate == null && model.get("aggregateRoot") != null) {
            aggregate = model;
        }
        var zdl = (Map<String, Object>) model.get("zdl");
        var aggregateRoot = aggregate != null ? (String) aggregate.get("aggregateRoot") : null;
        var rootEntity = aggregateRoot != null && zdl != null
                ? (Map<String, Object>) JSONPath.get(zdl, "$.allEntitiesAndEnums." + aggregateRoot)
                : null;
        if (aggregate == null || (JSONPath.get(aggregate, "lifecycle") == null && JSONPath.get(rootEntity, "lifecycle") == null)) {
            return true;
        }
        var commands = JSONPath.get(aggregate, "$.commands[*]", List.<Map<String, Object>>of());
        return commands.stream().noneMatch(command -> JSONPath.get(command, "$.transition.from") != null);
    };

    protected Function<Map<String, Object>, Boolean> skipEntityServiceTransitions = (model) -> {
        var entity = (Map<String, Object>) model.get("entity");
        var zdl = (Map<String, Object>) model.get("zdl");
        if (entity == null || zdl == null || JSONPath.get(entity, "lifecycle") == null) {
            return true;
        }
        var entityName = (String) entity.get("name");
        var services = JSONPath.get(zdl, "$.services[*]", List.<Map<String, Object>>of());
        for (var service : services) {
            var methods = JSONPath.get(service, "$.methods[*]", List.<Map<String, Object>>of());
            for (var method : methods) {
                if (entityName.equals(method.get("entity")) && JSONPath.get(method, "$.transition.from") != null) {
                    return false;
                }
            }
        }
        return true;
    };

    protected Function<Map<String, Object>, Boolean> skipDataSql = (model) -> persistence != PersistenceType.jpa;

    protected Function<Map<String, Object>, Boolean> skipEvents = (model) -> !includeEmitEventsImplementation;

    protected Function<Map<String, Object>, Boolean> skipEventsBus = (model) -> ((Collection) model.get("events")).isEmpty();

    protected Function<Map<String, Object>, Boolean> skipInput = (model) -> is(model, "inline");

    protected Function<Map<String, Object>, Boolean> skipJMolecules = (model) -> !useJMolecules;

    protected Function<Map<String, Object>, Boolean> skipListenerMappers = (model) ->
            JSONPath.get(model, "$.listenerGroup.mapperBindings", List.of()).isEmpty();

    protected Function<Map<String, Object>, Boolean> skipCleanArchitecture = (model) -> !(layout instanceof CleanHexagonalProjectLayout);

    protected Function<Map<String, Object>, Boolean> skipInfrastructurePackageInfo = (model) ->
            layout.moduleBasePackage.equals(layout.infrastructurePackage);

    @Override
    public List<Object> getTemplateHelpers(Generator generator) {
        var helpers = new ArrayList<>(super.getTemplateHelpers(generator));
        if (generator instanceof BackendApplicationDefaultGenerator backendGenerator) {
            helpers.add(new BackendApplicationQuarkusHelpers(backendGenerator));
        }
        return helpers;
    }

    public BackendApplicationQuarkusTemplates() {
        setTemplatesFolder("io/zenwave360/sdk/plugins/quarkus/BackendApplicationDefaultGenerator");

        var layoutNames = new ProjectLayout();

        this.addTemplate(this.aggregateTemplates, "src/main/java", "core/domain/common/Aggregate.java",
                layoutNames.entitiesPackage, "{{aggregate.name}}.java", JAVA, null, true);
        this.addTemplate(this.aggregateTemplates, "src/main/java", "core/domain/common/AggregateTransitions.java",
                layoutNames.entitiesPackage, "{{aggregateTransitionsClassName aggregate}}.java", JAVA, skipAggregateTransitions, true);
        this.addTemplate(this.domainEventsTemplates, "src/main/java", "core/domain/common/DomainEvent.java",
                layoutNames.domainEventsPackage, "{{event.name}}.java", JAVA, null, true);

        this.addTemplate(this.entityTemplates, "src/main/java", "core/domain/{{persistence}}/Entity.java",
                layoutNames.entitiesPackage, "{{entity.name}}.java", JAVA, skipEntity, false);
        this.addTemplate(this.entityTemplates, "src/main/java", "core/domain/common/EntityTransitions.java",
                layoutNames.entitiesPackage, "{{entityServiceTransitionsClassName entity}}.java", JAVA, skipEntityServiceTransitions, true);
        this.addTemplate(this.entityTemplates, "src/main/java", "core/outbound/{{persistence}}/{{style}}/EntityRepository.java",
                layoutNames.outboundRepositoryPackage, "{{entity.className}}Repository.java", JAVA, skipEntityRepository, true);
        this.addTemplate(this.entityTemplates, "src/main/java", "infrastructure/{{persistence}}/{{style}}/EntityRepositoryImpl.java",
                layoutNames.infrastructureRepositoryPackage, "{{entity.className}}RepositoryImpl.java", JAVA, skipEntityRepository, true);
        this.addTemplate(this.entityTemplates, "src/test/java", "infrastructure/{{persistence}}/{{style}}/BaseRepositoryIntegrationTest.java",
                layoutNames.infrastructureRepositoryCommonPackage, "BaseRepositoryIntegrationTest.java", JAVA, skipEntityRepository, true);
        this.addTemplate(this.entityTemplates, "src/test/java", "infrastructure/{{persistence}}/{{style}}/EntityRepositoryIntegrationTest.java",
                layoutNames.infrastructureRepositoryPackage, "{{entity.className}}RepositoryIntegrationTest.java", JAVA, skipEntityRepository, true);
        this.addTemplate(this.entityTemplates, "src/test/java", "infrastructure/{{persistence}}/{{style}}/inmemory/InMemory{{capitalizeFirst persistence}}Repository.java",
                layoutNames.infrastructureRepositoryPackage, "inmemory/InMemory{{capitalizeFirst persistence}}Repository.java", JAVA, skipEntityRepository, true);
        this.addTemplate(this.entityTemplates, "src/test/java", "infrastructure/{{persistence}}/{{style}}/inmemory/EntityRepositoryInMemory.java",
                layoutNames.infrastructureRepositoryPackage, "inmemory/{{entity.className}}RepositoryInMemory.java", JAVA, skipEntityRepository, true);

        this.addTemplate(this.entityTemplates, "src/test/resources", "data/{{persistence}}/entity/1.json",
                "", "data/{{persistence}}/{{entity.name}}/1.json", JSON, skipNonAggregateTestData, true);

        this.addTemplate(this.enumTemplates, "src/main/java", "core/domain/common/DomainEnum.java",
                layoutNames.entitiesPackage, "{{enum.name}}.java", JAVA, null, false);
        this.addTemplate(this.inputEnumTemplates, "src/main/java", "core/domain/common/InputEnum.java",
                layoutNames.inboundDtosPackage, "{{enum.name}}.java", JAVA, null, false);
        this.addTemplate(this.eventEnumTemplates, "src/main/java", "core/domain/common/EventEnum.java",
                layoutNames.domainEventsPackage, "{{enum.name}}.java", JAVA, skipInput, false);

        this.addTemplate(this.inputTemplates, "src/main/java", "core/inbound/dtos/InputOrOutput.java",
                layoutNames.inboundDtosPackage, "{{entity.className}}.java", JAVA, skipInput, false);
        this.addTemplate(this.outputTemplates, "src/main/java", "core/inbound/dtos/InputOrOutput.java",
                layoutNames.inboundDtosPackage, "{{entity.className}}.java", JAVA, null, false);

        this.addTemplate(this.serviceTemplates, "src/main/java", "core/inbound/Service.java",
                layoutNames.inboundPackage, "{{service.name}}.java", JAVA, null, false);
        this.addTemplate(this.serviceTemplates, "src/main/java", "core/implementation/{{style}}/ServiceImpl.java",
                layoutNames.coreImplementationPackage, "{{service.name}}Impl.java", JAVA, null, true);
        this.addTemplate(this.listenersByApiTemplates, "src/main/java", "adapters/events/EventListeners.java",
                layoutNames.adaptersEventsPackage, "{{listenerGroup.className}}.java", JAVA, null, true);
        this.addTemplate(this.listenersByApiTemplates, "src/main/java", "adapters/events/EventListenersMapper.java",
                layoutNames.adaptersEventsMappersPackage, "{{listenerGroup.className}}Mapper.java", JAVA, skipListenerMappers, true);
        this.asyncApiAdapterByApiTemplates.add(new TemplateInput(
                joinPath(getTemplatesFolder(), "src/main/java", "adapters/events/asyncapi/EventsMapper.java"),
                "{{asyncapiAdaptersModulePrefix}}src/main/java/" + layoutNames.adaptersEventsPackage + "/EventsMapper.java",
                JAVA).withSkipOverwrite(true));
        this.asyncApiAdapterByChannelTemplates.add(new TemplateInput(
                joinPath(getTemplatesFolder(), "src/main/java", "adapters/events/asyncapi/imperative/ConsumerService.java"),
                "{{asyncapiAdaptersModulePrefix}}src/main/java/" + layoutNames.adaptersEventsPackage + "/{{consumerServiceName}}.java",
                JAVA).withSkipOverwrite(true));

        this.addTemplate(this.singleTemplates, "src/main/java", "core/implementation/mappers/BaseMapper.java",
                layoutNames.coreImplementationMappersCommonPackage, "BaseMapper.java", JAVA, null, true);
        this.addTemplate(this.serviceTemplates, "src/main/java", "core/implementation/mappers/ServiceMapper.java",
                layoutNames.coreImplementationMappersPackage, "{{service.name}}Mapper.java", JAVA, null, true);
        this.addTemplate(this.serviceTemplates, "src/test/java", "core/implementation/{{persistence}}/{{style}}/ServiceTest.java",
                layoutNames.coreImplementationPackage, "{{service.name}}Test.java", JAVA, null, true);

        this.addTemplate(this.allServicesTemplates, "src/main/java", "core/implementation/mappers/EventsMapper.java",
                layoutNames.coreImplementationMappersPackage, "EventsMapper.java", JAVA, skipEvents, true);
        this.addTemplate(this.allServicesTemplates, "src/test/java", "config/RepositoriesInMemoryConfig.java",
                layoutNames.moduleConfigPackage, "RepositoriesInMemoryConfig.java", JAVA, null, true);
        this.addTemplate(this.allServicesTemplates, "src/test/java", "config/ServicesInMemoryConfig.java",
                layoutNames.moduleConfigPackage, "ServicesInMemoryConfig.java", JAVA, null, true);

        this.addTemplate(this.allDomainEventsTemplates, "src/main/java", "core/outbound/events/EventPublisher.java",
                layoutNames.outboundEventsPackage, "EventPublisher.java", JAVA, skipEventsBus, false);
        this.addTemplate(this.allDomainEventsTemplates, "src/main/java", "infrastructure/events/DefaultEventPublisher.java",
                layoutNames.infrastructureEventsPackage, "DefaultEventPublisher.java", JAVA, skipEventsBus, false);
        this.addTemplate(this.allDomainEventsTemplates, "src/test/java", "infrastructure/events/InMemoryEventPublisher.java",
                layoutNames.infrastructureEventsPackage, "InMemoryEventPublisher.java", JAVA, skipEventsBus, false);

        this.addTemplate(this.singleTemplates, "src/test/java", "config/TestDataLoader-{{persistence}}.java",
                layoutNames.configPackage, "TestDataLoader.java", JAVA, null, true);
        this.addTemplate(this.singleTemplates, "src/test/java", "config/DockerComposeInitializer-{{persistence}}.java",
                layoutNames.configPackage, "DockerComposeInitializer.java", JAVA, null, true);

        this.addTemplate(this.singleTemplates, "src/test/resources", "data.sql",
                null, "data.sql", SQL, skipDataSql, true);

        this.addTemplate(this.singleTemplates, "src/main/java", "core/inbound/dtos/package-info.java",
                layoutNames.inboundDtosPackage, "package-info.java", JAVA, null, true);
        this.addTemplate(this.singleTemplates, "src/main/java", "infrastructure/package-info.java",
                layoutNames.infrastructurePackage, "package-info.java", JAVA, skipInfrastructurePackageInfo, true);

        this.addTemplate(this.singleTemplates, "src/test/java", "ArchitectureTest.java",
                layoutNames.moduleBasePackage, "ArchitectureTest.java", JAVA, skipCleanArchitecture, true);
        this.addTemplate(this.singleTemplates, "src/test/java", "JMoleculesArchitectureTest.java",
                layoutNames.moduleBasePackage, "JMoleculesArchitectureTest.java", JAVA, skipJMolecules, true);
    }
}
