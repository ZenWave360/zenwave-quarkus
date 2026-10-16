package io.zenwave360.sdk.plugins.quarkus;

import io.zenwave360.sdk.MainGenerator;
import io.zenwave360.sdk.Plugin;
import io.zenwave360.sdk.options.PersistenceType;
import io.zenwave360.sdk.options.ProgrammingStyle;
import io.zenwave360.sdk.plugins.BackendApplicationDefaultPlugin;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

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

        Path root = Path.of(targetFolder, "src/main/java/io/example/orders");
        String entity = Files.readString(root.resolve("core/domain/Customer.java"));
        String repository = Files.readString(root.resolve("core/outbound/jpa/CustomerRepository.java"));
        String repositoryImpl = Files.readString(root.resolve("infrastructure/jpa/CustomerRepositoryImpl.java"));
        String service = Files.readString(root.resolve("core/inbound/CustomerService.java"));
        String serviceImpl = Files.readString(root.resolve("core/application/CustomerServiceImpl.java"));

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

    @Test
    void generatesMappersAndInProcessEvents() throws Exception {
        String targetFolder = "target/orders-backend";
        Plugin plugin = new BackendApplicationDefaultPlugin()
                .withZdlFile("classpath:zdl/customer-address-relational.zdl")
                .withTargetFolder(targetFolder)
                .withOption("templates", "new " + BackendApplicationQuarkusTemplates.class.getName())
                .withOption("basePackage", "io.example.orders")
                .withOption("persistence", PersistenceType.jpa)
                .withOption("style", ProgrammingStyle.imperative)
                .withOption("includeEmitEventsImplementation", true)
                .withOption("forceOverwrite", true)
                .withOption("haltOnFailFormatting", false);

        new MainGenerator().generate(plugin);

        Path root = Path.of(targetFolder, "src/main/java/io/example/orders");
        String serviceMapper = Files.readString(root.resolve("core/application/mappers/CustomerServiceMapper.java"));
        String eventsMapper = Files.readString(root.resolve("core/application/mappers/EventsMapper.java"));
        String baseMapper = Files.readString(root.resolve("core/application/mappers/BaseMapper.java"));
        String serviceImpl = Files.readString(root.resolve("core/application/CustomerServiceImpl.java"));
        String servicesConfig = Files.readString(Path.of(targetFolder, "src/test/java/io/example/orders/config/ServicesInMemoryConfig.java"));

        assertTrue(serviceMapper.contains("componentModel = \"cdi\""));
        assertTrue(eventsMapper.contains("componentModel = \"cdi\""));
        assertTrue(baseMapper.contains("componentModel = \"cdi\""));
        assertFalse(serviceMapper.contains("INSTANCE"));
        assertTrue(serviceImpl.contains("customerServiceMapper"));
        assertTrue(serviceImpl.contains("eventsMapper"));
        assertTrue(servicesConfig.contains("Mappers.getMapper(CustomerServiceMapper.class)"));
        assertTrue(servicesConfig.contains("Mappers.getMapper(EventsMapper.class)"));
        assertTrue(Files.exists(root.resolve("core/domain/AddressType.java")));
        assertTrue(Files.exists(root.resolve("core/inbound/dtos/CustomerInput.java")));
        assertFalse(serviceImpl.contains("org.springframework"));
        assertNoSpring(targetFolder);

        String eventsFolder = "target/orders-events";
        Plugin eventsPlugin = new BackendApplicationDefaultPlugin()
                .withZdlFile("classpath:zdl/local-domain-event.zdl")
                .withTargetFolder(eventsFolder)
                .withOption("templates", "new " + BackendApplicationQuarkusTemplates.class.getName())
                .withOption("basePackage", "io.example.orders")
                .withOption("persistence", PersistenceType.jpa)
                .withOption("style", ProgrammingStyle.imperative)
                .withOption("includeEmitEventsImplementation", true)
                .withOption("forceOverwrite", true)
                .withOption("haltOnFailFormatting", false);

        new MainGenerator().generate(eventsPlugin);

        Path eventsRoot = Path.of(eventsFolder, "src/main/java/io/example/orders");
        String publisher = Files.readString(eventsRoot.resolve("infrastructure/events/DefaultEventPublisher.java"));
        String eventType = Files.readString(eventsRoot.resolve("core/domain/events/CustomerCreated.java"));
        String inMemoryPublisher = Files.readString(Path.of(eventsFolder,
                "src/test/java/io/example/orders/infrastructure/events/InMemoryEventPublisher.java"));

        assertTrue(eventType.contains("class CustomerCreated"));
        assertTrue(publisher.contains("jakarta.enterprise.event.Event"));
        assertTrue(publisher.contains(".fire("));
        assertTrue(publisher.contains("Event<CustomerCreated>"));
        assertTrue(inMemoryPublisher.contains("void onCustomerCreated"));
        assertFalse(publisher.contains("org.springframework"));
        assertNoSpring(eventsFolder);
    }

    private static void assertNoSpring(String targetFolder) throws Exception {
        try (Stream<Path> paths = Files.walk(Path.of(targetFolder))) {
            for (Path path : paths.toList()) {
                String name = path.getFileName().toString();
                if (!name.endsWith(".java") && !name.endsWith(".sql") && !name.endsWith(".json")) {
                    continue;
                }
                String text = Files.readString(path);
                assertFalse(text.contains("org.springframework"), path.toString());
            }
        }
    }
}
