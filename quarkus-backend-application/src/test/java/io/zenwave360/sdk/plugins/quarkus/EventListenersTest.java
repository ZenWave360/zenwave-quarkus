package io.zenwave360.sdk.plugins.quarkus;

import io.zenwave360.sdk.MainGenerator;
import io.zenwave360.sdk.options.PersistenceType;
import io.zenwave360.sdk.options.ProgrammingStyle;
import io.zenwave360.sdk.plugins.BackendApplicationDefaultPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Internal and cross-module {@code @listener} bindings (zdl/orders-cross-module-listeners.zdl, upstream test model).
 */
class EventListenersTest {

    static final String TARGET = "target/orders-listeners";

    @BeforeAll
    static void generate() throws Exception {
        new MainGenerator().generate(new BackendApplicationDefaultPlugin()
                .withZdlFile("classpath:zdl/orders-cross-module-listeners.zdl")
                .withTargetFolder(TARGET)
                .withOption("templates", "new " + BackendApplicationQuarkusTemplates.class.getName())
                .withOption("persistence", PersistenceType.jpa)
                .withOption("style", ProgrammingStyle.imperative)
                .withOption("includeEmitEventsImplementation", true)
                .withOption("implementEventListeners", true)
                .withOption("forceOverwrite", true)
                .withOption("haltOnFailFormatting", false));
    }

    static String read(String simpleName) throws Exception {
        try (Stream<Path> files = Files.walk(Path.of(TARGET))) {
            Path file = files.filter(p -> p.getFileName().toString().equals(simpleName)).findFirst()
                    .orElseThrow(() -> new AssertionError("not generated: " + simpleName));
            return Files.readString(file);
        }
    }

    static long count(String source, String token) {
        return (source.length() - source.replace(token, "").length()) / token.length();
    }

    @Test
    void listenersRunAfterCommitInOwnTransaction() throws Exception {
        try (Stream<Path> files = Files.walk(Path.of(TARGET))) {
            var listeners = files.filter(p -> p.toString().endsWith("Listener.java")).toList();
            assertFalse(listeners.isEmpty(), "no *Listener.java generated");
            for (Path path : listeners) {
                String source = Files.readString(path);
                long observers = count(source, "@Observes(during = TransactionPhase.AFTER_SUCCESS)");
                long requiresNew = count(source, "@Transactional(Transactional.TxType.REQUIRES_NEW)");
                assertTrue(observers > 0, path.toString());
                assertEquals(observers, requiresNew, path.toString());
                assertFalse(source.contains("@Observes "), "plain synchronous observer: " + path);
                assertFalse(source.contains("org.springframework"), path.toString());
            }
        }
    }

    @Test
    void idPlaceholderNamesTheEntity() throws Exception {
        String all;
        try (Stream<Path> files = Files.walk(Path.of(TARGET))) {
            all = files.filter(p -> p.toString().endsWith("Listener.java")).map(p -> {
                try {
                    return Files.readString(p);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).reduce("", String::concat);
        }
        assertTrue(all.contains("TODO CUSTOM_REQUIRED: resolve the Order id for"), all);
        assertTrue(all.contains("(no event field is assumed to carry it)"));
    }

    @Test
    void serviceTestsCallZdlMethodNames() throws Exception {
        String test = read("OrdersServiceTest.java");
        assertFalse(test.contains(".createOrder("), "create test must call the ZDL method, not create<Entity>");
    }
}
