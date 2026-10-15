package io.zenwave360.sdk.plugins.quarkus;

import io.zenwave360.sdk.MainGenerator;
import io.zenwave360.sdk.Plugin;
import io.zenwave360.sdk.options.PersistenceType;
import io.zenwave360.sdk.options.ProgrammingStyle;
import io.zenwave360.sdk.plugins.BackendApplicationDefaultPlugin;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void generatesEntityRepositoryAndService() throws Exception {
        String targetFolder = "target/orders-slice";
        Plugin plugin = new BackendApplicationDefaultPlugin()
                .withZdlFile("classpath:zdl/customer-address-relational.zdl")
                .withTargetFolder(targetFolder)
                .withOption("templates", "new " + BackendApplicationQuarkusTemplates.class.getName())
                .withOption("basePackage", "io.example.orders")
                .withOption("persistence", PersistenceType.jpa)
                .withOption("style", ProgrammingStyle.imperative)
                .withOption("includeEmitEventsImplementation", false)
                .withOption("forceOverwrite", true)
                .withOption("haltOnFailFormatting", false);

        new MainGenerator().generate(plugin);

        Path root = Path.of(targetFolder, "src/main/kotlin/io/example/orders");
        String entity = Files.readString(root.resolve("core/domain/Customer.kt"));
        String repository = Files.readString(root.resolve("core/outbound/jpa/CustomerRepository.kt"));
        String repositoryImpl = Files.readString(root.resolve("infrastructure/jpa/CustomerRepositoryImpl.kt"));
        String service = Files.readString(root.resolve("core/inbound/CustomerService.kt"));
        String serviceImpl = Files.readString(root.resolve("core/application/CustomerServiceImpl.kt"));

        assertTrue(entity.contains("@Entity"));
        assertTrue(entity.contains("jakarta.persistence"));
        assertTrue(repository.contains("interface CustomerRepository"));
        assertFalse(repository.contains("JpaRepository"));
        assertTrue(repositoryImpl.contains("EntityManager"));
        assertTrue(repositoryImpl.contains("@ApplicationScoped"));
        assertTrue(service.contains("interface CustomerService"));
        assertTrue(serviceImpl.contains("@ApplicationScoped"));
        assertTrue(serviceImpl.contains("jakarta.transaction.Transactional"));
        assertTrue(serviceImpl.contains("customerRepository"));
        assertFalse((entity + repository + repositoryImpl + service + serviceImpl).contains("org.springframework"));
        assertFalse((service + serviceImpl).contains("Pageable"));
        assertFalse(serviceImpl.contains("pageable"));
        assertFalse((service + serviceImpl).contains("Page<"));
    }
}
