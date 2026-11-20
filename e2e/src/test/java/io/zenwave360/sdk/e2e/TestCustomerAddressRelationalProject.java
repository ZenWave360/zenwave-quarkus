package io.zenwave360.sdk.e2e;

import org.junit.jupiter.api.Test;

/**
 * Fresh generation on the archetype scaffold: compile everything, run ArchitectureTest.
 * Generated service and HTTP tests are TODO skeletons here, so only the architecture rules run.
 */
class TestCustomerAddressRelationalProject {

    @Test
    void generatesCompilesAndRespectsArchitecture() throws Exception {
        var project = ScaffoldProject.copy("customer-address-relational", "zdl/customer-address-relational.zdl");
        int exitCode = project.mvn(true, "test", "test=ArchitectureTest", "surefire.failIfNoSpecifiedTests=false");
        ScaffoldProject.assertSuccess(exitCode);
    }
}
