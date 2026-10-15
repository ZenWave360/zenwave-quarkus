package io.zenwave360.sdk.plugins.quarkus;

import io.zenwave360.sdk.generators.Generator;
import io.zenwave360.sdk.options.PersistenceType;
import io.zenwave360.sdk.options.ProgrammingStyle;
import io.zenwave360.sdk.plugins.BackendApplicationDefaultGenerator;
import io.zenwave360.sdk.utils.JSONPath;
import io.zenwave360.sdk.zdl.ProjectTemplates;
import io.zenwave360.sdk.zdl.layouts.ProjectLayout;
import io.zenwave360.sdk.zdl.utils.ZDLFindUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.zenwave360.sdk.templating.OutputFormatType.KOTLIN;
import static io.zenwave360.sdk.zdl.utils.ZDLFindUtils.is;

/**
 * Quarkus templates for {@code BackendApplicationDefaultPlugin}, selected with the {@code templates} option.
 */
public class BackendApplicationQuarkusTemplates extends ProjectTemplates {

    public PersistenceType persistence = PersistenceType.jpa;

    public ProgrammingStyle style = ProgrammingStyle.imperative;

    public boolean includeEmitEventsImplementation = true;

    protected Function<Map<String, Object>, Boolean> skipEntity = (model) -> is(model, "vo", "input");

    protected Function<Map<String, Object>, Boolean> skipEntityRepository = (model) -> is(model, "persistence")
            || !(is(model, "aggregate") || is(model, "lifecycle")
                    || ZDLFindUtils.isAggregateRoot(JSONPath.get(model, "zdl"), JSONPath.get(model, "$.entity.name")));

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

        this.addTemplate(this.entityTemplates, "src/main/kotlin", "core/domain/{{persistence}}/Entity.kt",
                layoutNames.entitiesPackage, "{{entity.name}}.kt", KOTLIN, skipEntity, false);
        this.addTemplate(this.entityTemplates, "src/main/kotlin", "core/outbound/{{persistence}}/{{style}}/EntityRepository.kt",
                layoutNames.outboundRepositoryPackage, "{{entity.className}}Repository.kt", KOTLIN, skipEntityRepository, true);
        this.addTemplate(this.entityTemplates, "src/main/kotlin", "infrastructure/{{persistence}}/{{style}}/EntityRepositoryImpl.kt",
                layoutNames.infrastructureRepositoryPackage, "{{entity.className}}RepositoryImpl.kt", KOTLIN, skipEntityRepository, true);

        this.addTemplate(this.serviceTemplates, "src/main/kotlin", "core/inbound/Service.kt",
                layoutNames.inboundPackage, "{{service.name}}.kt", KOTLIN, null, false);
        this.addTemplate(this.serviceTemplates, "src/main/kotlin", "core/implementation/{{style}}/ServiceImpl.kt",
                layoutNames.coreImplementationPackage, "{{service.name}}Impl.kt", KOTLIN, null, true);
    }
}
