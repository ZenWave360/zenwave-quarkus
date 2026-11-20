package io.zenwave360.sdk.plugins.quarkus;

import io.zenwave360.sdk.MainGenerator;
import io.zenwave360.sdk.options.PersistenceType;
import io.zenwave360.sdk.options.ProgrammingStyle;
import io.zenwave360.sdk.plugins.BackendApplicationDefaultPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Natural ids, @auditing and @json value objects (zdl/order-fulfillment.zdl, SimpleDomainProjectLayout).
 */
class OrderFulfillmentFeaturesTest {

    static final String TARGET = "target/order-fulfillment";
    static final Path MAIN = Path.of(TARGET, "src/main/java/io/zenwave360/example/orderfulfillment");
    static final Path TEST = Path.of(TARGET, "src/test/java/io/zenwave360/example/orderfulfillment");

    @BeforeAll
    static void generate() throws Exception {
        new MainGenerator().generate(new BackendApplicationDefaultPlugin()
                .withZdlFile("classpath:zdl/order-fulfillment.zdl")
                .withTargetFolder(TARGET)
                .withOption("templates", "new " + BackendApplicationQuarkusTemplates.class.getName())
                .withOption("persistence", PersistenceType.jpa)
                .withOption("style", ProgrammingStyle.imperative)
                .withOption("includeEmitEventsImplementation", true)
                .withOption("forceOverwrite", true)
                .withOption("haltOnFailFormatting", false));
    }

    @Test
    void repositoryImplementsNaturalIdFinder() throws Exception {
        String port = Files.readString(MAIN.resolve("OrderRepository.java"));
        String impl = Files.readString(MAIN.resolve("OrderRepositoryImpl.java"));
        assertTrue(port.contains("findByOrderNumber(String orderNumber)"));
        assertTrue(impl.contains("public java.util.Optional<Order> findByOrderNumber(String orderNumber)"));
        assertTrue(impl.contains("where orderNumber = :orderNumber"));
    }

    @Test
    void auditingEntitiesUseListenerAndAuditable() throws Exception {
        String entity = Files.readString(MAIN.resolve("domain/Order.java"));
        assertTrue(entity.contains("@EntityListeners(AuditingEntityListener.class)"));
        assertTrue(entity.contains("implements Serializable, Auditable"));
        assertTrue(entity.contains("markCreatedBy(String auditor)"));
        assertTrue(Files.exists(MAIN.resolve("domain/AuditingEntityListener.java")));
        assertTrue(Files.exists(MAIN.resolve("domain/AuditorProvider.java")));
        String listener = Files.readString(MAIN.resolve("domain/AuditingEntityListener.java"));
        assertFalse(listener.contains("java.lang.reflect"));
        assertFalse(listener.contains("VarHandle"));
        // CDI injection keeps the AuditorProvider bean from ArC unused-bean removal
        assertFalse(listener.contains("CDI.current()"));
        assertTrue(listener.contains("Instance<AuditorProvider>"));
    }

    @Test
    void jsonValueObjectsAreEqualByValue() throws Exception {
        String item = Files.readString(MAIN.resolve("domain/OrderItem.java"));
        assertTrue(item.contains("Objects.equals(productId, other.productId)"));
        assertTrue(item.contains("Objects.hash(productId, productName, quantity, unitPrice)"));
        assertFalse(item.contains("@Entity"));
    }

    @Test
    void repositoryTestsRollBack() throws Exception {
        String base = Files.readString(TEST.resolve("BaseRepositoryIntegrationTest.java"));
        String repoIt = Files.readString(TEST.resolve("OrderRepositoryIntegrationTest.java"));
        assertTrue(base.contains("@TestTransaction"));
        assertFalse(base.contains("jakarta.transaction.Transactional"));
        assertTrue(repoIt.contains("@QuarkusTest"));
        assertTrue(repoIt.contains("@TestTransaction"));
    }
}
