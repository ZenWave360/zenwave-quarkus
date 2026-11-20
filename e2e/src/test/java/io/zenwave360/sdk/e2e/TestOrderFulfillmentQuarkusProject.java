package io.zenwave360.sdk.e2e;

import org.junit.jupiter.api.Test;

/**
 * Hand-finished application: regenerate with the current templates (hand-edited files are kept),
 * then build and run all tests, including *IT, against Dev Services (Postgres, Kafka).
 * Without Docker only test-compile runs.
 */
class TestOrderFulfillmentQuarkusProject {

    @Test
    void regeneratesAndPassesAllTests() throws Exception {
        var project = ScaffoldProject.copy("order-fulfillment-quarkus", "zdl/order-fulfillment.zdl");
        int exitCode = ScaffoldProject.dockerAvailable()
                ? project.mvn(true, "verify", "skipITs=false")
                : project.mvn(true, "test-compile");
        ScaffoldProject.assertSuccess(exitCode);
    }
}
