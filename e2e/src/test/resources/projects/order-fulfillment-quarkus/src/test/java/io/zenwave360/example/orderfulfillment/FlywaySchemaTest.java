package io.zenwave360.example.orderfulfillment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.util.Map;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

/**
 * Prod-like schema: Flyway migrations applied, Hibernate validates the entities against them.
 * Startup fails if an entity and db/migration drift apart.
 */
@QuarkusTest
@TestProfile(FlywaySchemaTest.FlywayProfile.class)
public class FlywaySchemaTest {

    public static class FlywayProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.flyway.clean-disabled", "false",
                    "quarkus.flyway.clean-at-start", "true",
                    "quarkus.flyway.migrate-at-start", "true",
                    "quarkus.hibernate-orm.schema-management.strategy", "validate",
                    "quarkus.hibernate-orm.sql-load-script", "no-file");
        }
    }

    @Inject
    Flyway flyway;

    @Test
    void migrationsMatchEntities() {
        assertTrue(flyway.info().applied().length > 0);
    }
}
