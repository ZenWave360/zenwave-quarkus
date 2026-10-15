package io.zenwave360.sdk.plugins.quarkus;

import io.zenwave360.sdk.generators.Generator;
import io.zenwave360.sdk.plugins.OpenAPIControllersGenerator;
import io.zenwave360.sdk.utils.JSONPath;
import io.zenwave360.sdk.zdl.ProjectTemplates;
import io.zenwave360.sdk.zdl.layouts.ProjectLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.zenwave360.sdk.templating.OutputFormatType.KOTLIN;

/**
 * Quarkus templates for {@code OpenAPIControllersPlugin}, selected with the {@code templates} option.
 *
 * <p>Same target files as
 * {@code ../zenwave-sdk/plugins/customizations/kotlin-backend-application}
 * ({@code OpenAPIControllersKotlinTemplates}), with {@code web/jaxrs} instead of {@code web/{{webFlavor}}}.
 */
public class OpenAPIControllersQuarkusTemplates extends ProjectTemplates {

    public boolean includeControllerTests = true;

    public Function<Map<String, Object>, Boolean> skipControllerTests = (model) -> !includeControllerTests;

    public Function<Map<String, Object>, Boolean> skipPatchMap = (model) ->
            JSONPath.get(model, "$.openapi.paths[*].patch", List.of()).isEmpty();

    public OpenAPIControllersQuarkusTemplates() {
        setTemplatesFolder("io/zenwave360/sdk/plugins/quarkus/OpenAPIControllersGenerator");

        var layoutNames = new ProjectLayout();
        this.addTemplate(this.singleTemplates, "src/main/kotlin", "web/common/PatchMap.kt",
                layoutNames.adaptersWebCommonPackage, "PatchMap.kt", KOTLIN, skipPatchMap, false);
        this.addTemplate(this.singleTemplates, "src/main/kotlin", "web/mappers/BaseMapper.kt",
                layoutNames.adaptersWebMappersCommonPackage, "BaseMapper.kt", KOTLIN, null, false);
        this.addTemplate(this.serviceTemplates, "src/main/kotlin", "web/mappers/ServiceDTOsMapper.kt",
                layoutNames.adaptersWebMappersPackage, "{{serviceName}}DTOsMapper.kt", KOTLIN, null, true);
        this.addTemplate(this.serviceTemplates, "src/main/kotlin", "web/jaxrs/ServiceApiController.kt",
                layoutNames.adaptersWebPackage, "{{serviceName}}ApiController.kt", KOTLIN, null, false);
        this.addTemplate(this.serviceTemplates, "src/test/kotlin", "web/jaxrs/ServiceApiControllerTest.kt",
                layoutNames.adaptersWebPackage, "{{serviceName}}ApiControllerTest.kt", KOTLIN, skipControllerTests, true);
    }

    @Override
    public List<Object> getTemplateHelpers(Generator generator) {
        var helpers = new ArrayList<>(super.getTemplateHelpers(generator));
        if (generator instanceof OpenAPIControllersGenerator openAPIControllersGenerator) {
            helpers.add(new OpenAPIControllersQuarkusHelpers(
                    openAPIControllersGenerator.openApiModelNamePrefix, openAPIControllersGenerator.openApiModelNameSuffix));
        }
        return helpers;
    }
}
