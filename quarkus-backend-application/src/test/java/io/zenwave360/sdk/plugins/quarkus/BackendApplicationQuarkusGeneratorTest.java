package io.zenwave360.sdk.plugins.quarkus;

import io.zenwave360.sdk.MainGenerator;
import io.zenwave360.sdk.Plugin;
import io.zenwave360.sdk.options.PersistenceType;
import io.zenwave360.sdk.options.ProgrammingStyle;
import io.zenwave360.sdk.plugins.BackendApplicationDefaultPlugin;
import org.junit.jupiter.api.Test;

class BackendApplicationQuarkusGeneratorTest {

    @Test
    void loadsZdlAndRunsQuarkusTemplates() throws Exception {
        Plugin plugin = new BackendApplicationDefaultPlugin()
                .withZdlFile("classpath:zdl/customer-address-relational.zdl")
                .withTargetFolder("target/orders")
                .withOption("templates", "new " + BackendApplicationQuarkusTemplates.class.getName())
                .withOption("basePackage", "io.example.orders")
                .withOption("persistence", PersistenceType.jpa)
                .withOption("style", ProgrammingStyle.imperative)
                .withOption("forceOverwrite", true)
                .withOption("haltOnFailFormatting", false);

        new MainGenerator().generate(plugin);
    }
}
