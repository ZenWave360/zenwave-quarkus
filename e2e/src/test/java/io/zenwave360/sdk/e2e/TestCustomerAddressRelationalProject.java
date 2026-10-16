package io.zenwave360.sdk.e2e;

import io.zenwave360.sdk.MainGenerator;
import io.zenwave360.sdk.options.DatabaseType;
import io.zenwave360.sdk.options.PersistenceType;
import io.zenwave360.sdk.options.ProgrammingStyle;
import io.zenwave360.sdk.plugins.AsyncAPIGeneratorPlugin;
import io.zenwave360.sdk.plugins.BackendApplicationDefaultPlugin;
import io.zenwave360.sdk.plugins.OpenAPIControllersPlugin;
import io.zenwave360.sdk.plugins.SpringWebTestClientPlugin;
import io.zenwave360.sdk.plugins.ZDLToAsyncAPIPlugin;
import io.zenwave360.sdk.plugins.ZDLToOpenAPIPlugin;
import io.zenwave360.sdk.plugins.quarkus.BackendApplicationQuarkusTemplates;
import io.zenwave360.sdk.plugins.quarkus.OpenAPIControllersQuarkusTemplates;
import io.zenwave360.sdk.plugins.quarkus.QuarkusHttpTestTemplates;
import io.zenwave360.sdk.plugins.quarkus.SmallRyeTemplates;
import io.zenwave360.sdk.testutils.MavenCompiler;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestCustomerAddressRelationalProject {

    private static final String BASE_PACKAGE = "io.example.orders";

    @Test
    void compilesGeneratedQuarkusProject() throws Exception {
        Path source = Path.of("src/test/resources/projects/customer-address-relational");
        Path target = Path.of("target/projects/customer-address-relational");
        deleteRecursively(target);
        copyProject(source, target);
        assertTrue(Files.exists(target.resolve("pom.xml")));

        String targetFolder = target.toString();
        String zdlFile = "classpath:zdl/customer-address-relational.zdl";
        String openApiFile = targetFolder + "/src/main/resources/apis/openapi.yml";
        String asyncApiFile = targetFolder + "/src/main/resources/apis/asyncapi.yml";

        new MainGenerator().generate(new ZDLToOpenAPIPlugin()
                .withZdlFile(zdlFile)
                .withTargetFolder(targetFolder)
                .withOption("idType", "integer")
                .withOption("idTypeFormat", "int64")
                .withOption("targetFile", "/src/main/resources/apis/openapi.yml"));

        new MainGenerator().generate(new ZDLToAsyncAPIPlugin()
                .withZdlFile(zdlFile)
                .withTargetFolder(targetFolder)
                .withOption("asyncapiVersion", "v3")
                .withOption("idType", "integer")
                .withOption("idTypeFormat", "int64")
                .withOption("targetFile", "/src/main/resources/apis/asyncapi.yml"));

        new MainGenerator().generate(new BackendApplicationDefaultPlugin()
                .withZdlFile(zdlFile)
                .withTargetFolder(targetFolder)
                .withOption("templates", "new " + BackendApplicationQuarkusTemplates.class.getName())
                .withOption("basePackage", BASE_PACKAGE)
                .withOption("persistence", PersistenceType.jpa)
                .withOption("databaseType", DatabaseType.postgresql)
                .withOption("style", ProgrammingStyle.imperative)
                .withOption("includeEmitEventsImplementation", true)
                .withOption("forceOverwrite", true)
                .withOption("haltOnFailFormatting", false));

        new MainGenerator().generate(new OpenAPIControllersPlugin()
                .withApiFile(openApiFile)
                .withTargetFolder(targetFolder)
                .withOption("zdlFile", zdlFile)
                .withOption("templates", "new " + OpenAPIControllersQuarkusTemplates.class.getName())
                .withOption("basePackage", BASE_PACKAGE)
                .withOption("openApiModelNameSuffix", "DTO")
                .withOption("style", ProgrammingStyle.imperative)
                .withOption("forceOverwrite", true)
                .withOption("haltOnFailFormatting", false));

        new MainGenerator().generate(new SpringWebTestClientPlugin()
                .withApiFile(openApiFile)
                .withTargetFolder(targetFolder)
                .withOption("zdlFile", zdlFile)
                .withOption("templates", "new " + QuarkusHttpTestTemplates.class.getName())
                .withOption("basePackage", BASE_PACKAGE));

        new MainGenerator().generate(new AsyncAPIGeneratorPlugin()
                .withApiFile(asyncApiFile)
                .withTargetFolder(targetFolder)
                .withOption("templates", SmallRyeTemplates.class.getName())
                .withOption("modelPackage", BASE_PACKAGE + ".core.outbound.events.dtos")
                .withOption("producerApiPackage", BASE_PACKAGE + ".core.outbound.events")
                .withOption("consumerApiPackage", BASE_PACKAGE + ".adapters.commands")
                .withOption("jsonschema2pojo.useLongIntegers", "true")
                .withOption("jsonschema2pojo.isUseJakartaValidation", "true")
                .withOption("haltOnFailFormatting", false));

        int exitCode = MavenCompiler.compile(new File(targetFolder), "openapi.skip=false");
        assertEquals(0, exitCode);
    }

    private static void copyProject(Path source, Path target) throws IOException {
        try (Stream<Path> walk = Files.walk(source)) {
            for (Path path : walk.toList()) {
                Path relative = source.relativize(path);
                if (relative.getNameCount() > 0 && "target".equals(relative.getName(0).toString())) {
                    continue;
                }
                Path dest = target.resolve(relative.toString());
                if (Files.isDirectory(path)) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(path, dest);
                }
            }
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(root)) {
            for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }
}
