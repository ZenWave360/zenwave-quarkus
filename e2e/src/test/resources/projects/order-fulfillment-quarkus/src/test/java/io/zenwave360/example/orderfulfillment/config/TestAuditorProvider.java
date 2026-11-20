package io.zenwave360.example.orderfulfillment.config;

import io.zenwave360.example.orderfulfillment.domain.AuditorProvider;
import jakarta.enterprise.context.ApplicationScoped;

/** Test-only auditor: proves the JPA auditing listener gets CDI beans injected. */
@ApplicationScoped
public class TestAuditorProvider implements AuditorProvider {

    public static final String AUDITOR = "test-auditor";

    @Override
    public String currentAuditor() {
        return AUDITOR;
    }
}
