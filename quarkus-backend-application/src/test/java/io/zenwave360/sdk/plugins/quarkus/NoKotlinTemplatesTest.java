package io.zenwave360.sdk.plugins.quarkus;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Generated Quarkus code is Java only: no Kotlin templates ship in this jar.
 */
class NoKotlinTemplatesTest {

    @Test
    void templatesAreJavaOnly() throws Exception {
        Path templates = Path.of("src/main/resources/io/zenwave360/sdk/plugins/quarkus");
        try (Stream<Path> walk = Files.walk(templates)) {
            var kotlin = walk.map(Path::toString)
                    .filter(p -> p.endsWith(".kt.hbs") || p.contains("kotlin"))
                    .toList();
            assertEquals(java.util.List.of(), kotlin);
        }
    }
}
