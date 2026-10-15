package io.zenwave360.sdk.plugins.quarkus;

import io.zenwave360.sdk.generators.Generator;
import io.zenwave360.sdk.plugins.SpringWebTestClientGenerator;
import io.zenwave360.sdk.plugins.SpringWebTestClientTemplates;
import io.zenwave360.sdk.templating.TemplateInput;

import java.util.ArrayList;
import java.util.List;

import static io.zenwave360.sdk.templating.OutputFormatType.KOTLIN;

/**
 * Quarkus templates for {@code SpringWebTestClientPlugin}, selected with the {@code templates} option.
 *
 * <p>Same entry points as
 * {@code ../zenwave-sdk/plugins/customizations/kotlin-backend-application}
 * ({@code SpringWebTestClientKotlinTemplates}).
 */
public class QuarkusHttpTestTemplates extends SpringWebTestClientTemplates {

    public QuarkusHttpTestTemplates() {
        setTemplatesFolder("io/zenwave360/sdk/plugins/quarkus/SpringWebTestClientGenerator");
    }

    @Override
    public List<Object> getTemplateHelpers(Generator generator) {
        var helpers = new ArrayList<>(super.getTemplateHelpers(generator));
        if (generator instanceof SpringWebTestClientGenerator webTestClientGenerator) {
            helpers.add(new QuarkusHttpTestHelpers(
                    webTestClientGenerator.openApiModelNamePrefix, webTestClientGenerator.openApiModelNameSuffix));
        }
        return helpers;
    }

    @Override
    public TemplateInput partialTemplate() {
        return new TemplateInput()
                .withTemplateLocation(joinPath(templatesFolder, "partials/Operation.kt"))
                .withTargetFile(joinPath("src/test/kotlin", "{{asPackageFolder testsPackage}}/Operation.kt"))
                .withMimeType(KOTLIN)
                .withSkipOverwrite(false);
    }

    @Override
    public TemplateInput baseTestClassTemplate() {
        return new TemplateInput()
                .withTemplateLocation(joinPath(templatesFolder, "BaseQuarkusHttpTest.kt"))
                .withTargetFile(joinPath("src/test/kotlin", "{{asPackageFolder baseTestClassPackage}}/BaseQuarkusHttpTest.kt"))
                .withMimeType(KOTLIN)
                .withSkipOverwrite(true);
    }

    @Override
    public TemplateInput businessFlowTestTemplate() {
        return new TemplateInput()
                .withTemplateLocation(joinPath(templatesFolder, "BusinessFlowTest.kt"))
                .withTargetFile(joinPath("src/test/kotlin", "{{asPackageFolder testsPackage}}/{{businessFlowTestName}}.kt"))
                .withMimeType(KOTLIN)
                .withSkipOverwrite(false);
    }

    @Override
    public TemplateInput serviceTestTemplate() {
        return new TemplateInput()
                .withTemplateLocation(joinPath(templatesFolder, "ServiceIT.kt"))
                .withTargetFile(joinPath("src/test/kotlin", "{{asPackageFolder testsPackage}}/{{serviceName}}{{testSuffix}}.kt"))
                .withMimeType(KOTLIN)
                .withSkipOverwrite(false);
    }

    @Override
    public TemplateInput operationTestTemplate() {
        return new TemplateInput()
                .withTemplateLocation(joinPath(templatesFolder, "OperationIT.kt"))
                .withTargetFile(joinPath("src/test/kotlin", "{{asPackageFolder testsPackage}}/{{serviceName}}/{{asJavaTypeName operationId}}{{testSuffix}}.kt"))
                .withMimeType(KOTLIN)
                .withSkipOverwrite(false);
    }
}
