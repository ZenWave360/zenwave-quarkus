package io.zenwave360.sdk.plugins.quarkus;

import io.zenwave360.sdk.MainGenerator;
import io.zenwave360.sdk.Plugin;
import io.zenwave360.sdk.options.ProgrammingStyle;
import io.zenwave360.sdk.plugins.OpenAPIControllersPlugin;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAPIControllersQuarkusGeneratorTest {

    @Test
    void generatesJaxrsController() throws Exception {
        String targetFolder = "target/orders-web";
        Plugin plugin = new OpenAPIControllersPlugin()
                .withApiFile("classpath:openapi/openapi.yml")
                .withOption("zdlFile", "classpath:zdl/customer-address-relational.zdl")
                .withOption("templates", "new " + OpenAPIControllersQuarkusTemplates.class.getName())
                .withOption("basePackage", "io.example.orders")
                .withOption("openApiModelNameSuffix", "DTO")
                .withOption("style", ProgrammingStyle.imperative)
                .withOption("forceOverwrite", true)
                .withOption("haltOnFailFormatting", false)
                .withTargetFolder(targetFolder);

        new MainGenerator().generate(plugin);

        Path root = Path.of(targetFolder, "src/main/java/io/example/orders/adapters/web");
        String controller = Files.readString(root.resolve("CustomerApiController.java"));
        String mapper = Files.readString(root.resolve("mappers/CustomerDTOsMapper.java"));

        assertTrue(controller.contains("@ApplicationScoped"));
        assertTrue(controller.contains("@Path"));
        assertTrue(controller.contains("@POST"));
        assertTrue(controller.contains("@GET"));
        assertTrue(controller.contains("jakarta.ws.rs"));
        assertTrue(controller.contains("customerService"));
        assertTrue(controller.contains("listCustomers(page, limit)"));
        assertFalse(controller.contains("pageOf("));
        assertFalse(controller.contains("Pageable"));
        assertFalse(controller.contains("org.springframework"));
        assertTrue(mapper.contains("componentModel = \"cdi\""));
        assertFalse(mapper.contains("INSTANCE"));
        assertFalse(mapper.contains("Page<"));
        assertTrue(Files.exists(Path.of(targetFolder, "src/test/java/io/example/orders/adapters/web/CustomerApiControllerTest.java")));
    }
}
