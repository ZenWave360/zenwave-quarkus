package io.zenwave360.example.orderfulfillment;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;

/**
 * Base class for repository integration tests. Each test runs in a transaction that is rolled back.
 */
@QuarkusTest
@TestTransaction
public abstract class BaseRepositoryIntegrationTest {}
