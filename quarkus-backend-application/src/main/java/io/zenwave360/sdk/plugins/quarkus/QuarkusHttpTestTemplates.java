package io.zenwave360.sdk.plugins.quarkus;

import io.zenwave360.sdk.generators.Generator;
import io.zenwave360.sdk.plugins.SpringWebTestClientGenerator;
import io.zenwave360.sdk.plugins.SpringWebTestClientTemplates;
import io.zenwave360.sdk.templating.TemplateInput;

import java.util.ArrayList;
import java.util.List;

import static io.zenwave360.sdk.templating.OutputFormatType.JAVA;

/**
 * Quarkus templates for {@code SpringWebTestClientPlugin}, selected with the {@code templates} option.
 *
 * <p>Same entry points as
 * {@code ../zenwave-sdk/plugins/customizations/kotlin-backend-application}
 * ({@code SpringWebTestClientKotlinTemplates}). Generated sources are Java.
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
                .withTemplateLocation(joinPath(templatesFolder, "partials/Operation.java"))
                .withTargetFile(joinPath("src/test/java", "{{asPackageFolder testsPackage}}/Operation.java"))
                .withMimeType(JAVA)
                .withSkipOverwrite(false);
    }

    @Override
    public TemplateInput baseTestClassTemplate() {
        return new TemplateInput()
                .withTemplateLocation(joinPath(templatesFolder, "BaseQuarkusHttpTest.java"))
                .withTargetFile(joinPath("src/test/java", "{{asPackageFolder baseTestClassPackage}}/BaseQuarkusHttpTest.java"))
                .withMimeType(JAVA)
                .withSkipOverwrite(true);
    }

    @Override
    public TemplateInput businessFlowTestTemplate() {
        return new TemplateInput()
                .withTemplateLocation(joinPath(templatesFolder, "BusinessFlowTest.java"))
                .withTargetFile(joinPath("src/test/java", "{{asPackageFolder testsPackage}}/{{businessFlowTestName}}.java"))
                .withMimeType(JAVA)
                .withSkipOverwrite(true);
    }

    @Override
    public TemplateInput serviceTestTemplate() {
        return new TemplateInput()
                .withTemplateLocation(joinPath(templatesFolder, "ServiceIT.java"))
                .withTargetFile(joinPath("src/test/java", "{{asPackageFolder testsPackage}}/{{serviceName}}{{testSuffix}}.java"))
                .withMimeType(JAVA)
                .withSkipOverwrite(true);
    }

    @Override
    public TemplateInput operationTestTemplate() {
        return new TemplateInput()
                .withTemplateLocation(joinPath(templatesFolder, "OperationIT.java"))
                .withTargetFile(joinPath("src/test/java", "{{asPackageFolder testsPackage}}/{{serviceName}}/{{asJavaTypeName operationId}}{{testSuffix}}.java"))
                .withMimeType(JAVA)
                .withSkipOverwrite(true);
    }
}
