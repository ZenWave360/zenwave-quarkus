package io.zenwave360.example.orderfulfillment.domain;

/**
 * Supplies the user written to {@code createdBy} / {@code lastModifiedBy} of {@code @auditing} entities.
 *
 * <p>Provide one CDI bean implementing this interface, for example from {@code io.quarkus.security.identity.SecurityIdentity}.
 * Without a bean, {@link AuditingEntityListener} writes {@value AuditingEntityListener#DEFAULT_AUDITOR}.
 */
public interface AuditorProvider {

    String currentAuditor();
}
