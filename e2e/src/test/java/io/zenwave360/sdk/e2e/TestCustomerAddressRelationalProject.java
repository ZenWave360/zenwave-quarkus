package io.zenwave360.sdk.e2e;

import org.junit.jupiter.api.Test;

/**
 * Fresh generation on the archetype scaffold: compile everything, run ArchitectureTest and,
 * with Docker, boot the app and drive it through Kafka (CustomerCommandsKafkaTest).
 * Generated service and HTTP tests are TODO skeletons here, so they do not run.
 */
class TestCustomerAddressRelationalProject {

    @Test
    void generatesBootsAndRespectsArchitecture() throws Exception {
        var project = ScaffoldProject.copy("customer-address-relational", "zdl/customer-address-relational.zdl");
        var tests = ScaffoldProject.dockerAvailable() ? "ArchitectureTest,CustomerCommandsKafkaTest" : "ArchitectureTest";
        int exitCode = project.mvn(true, "test", "test=" + tests, "surefire.failIfNoSpecifiedTests=false");
        ScaffoldProject.assertSuccess(exitCode);
    }
}
