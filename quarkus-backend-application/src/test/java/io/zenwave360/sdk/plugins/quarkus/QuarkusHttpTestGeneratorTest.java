package io.zenwave360.sdk.plugins.quarkus;

import io.zenwave360.sdk.MainGenerator;
import io.zenwave360.sdk.Plugin;
import io.zenwave360.sdk.plugins.SpringWebTestClientPlugin;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuarkusHttpTestGeneratorTest {

    @Test
    void generatesQuarkusHttpTest() throws Exception {
        String targetFolder = "target/orders-http";
        Plugin plugin = new SpringWebTestClientPlugin()
                .withApiFile("classpath:openapi/openapi.yml")
                .withOption("templates", "new " + QuarkusHttpTestTemplates.class.getName())
                .withOption("basePackage", "io.example.orders")
                .withOption("testsPackage", "io.example.orders.adapters.web")
                .withOption("baseTestClassPackage", "io.example.orders.adapters.web")
                .withTargetFolder(targetFolder);

        new MainGenerator().generate(plugin);

        Path root = Path.of(targetFolder, "src/test/kotlin/io/example/orders/adapters/web");
        String base = Files.readString(root.resolve("BaseQuarkusHttpTest.kt"));
        String test = Files.readString(root.resolve("CustomerApiIntegrationTest.kt"));

        assertTrue(base.contains("@QuarkusTest"));
        assertTrue(base.contains("java.net.http.HttpClient"));
        assertTrue(test.contains("@QuarkusTest") || base.contains("@QuarkusTest"));
        assertTrue(test.contains("@TestHTTPEndpoint(CustomerApiController::class)"));
        assertTrue(test.contains("@TestHTTPResource"));
        assertTrue(test.contains("httpClient.send"));
        assertTrue(test.contains("java.net.http.HttpRequest") || test.contains("HttpRequest.newBuilder"));
        assertTrue(test.contains("fun testCreateCustomer_201"));
        assertTrue(test.contains("fun testListCustomers_200"));
        assertFalse((base + test).contains("org.springframework"));
        assertFalse(test.contains("WebTestClient"));
    }
}
