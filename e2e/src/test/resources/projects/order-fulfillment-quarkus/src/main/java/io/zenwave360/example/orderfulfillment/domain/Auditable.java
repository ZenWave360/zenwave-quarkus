package io.zenwave360.example.orderfulfillment.domain;

/**
 * Implemented by {@code @auditing} entities. Called by {@link AuditingEntityListener}.
 */
public interface Auditable {

    void markCreatedBy(String auditor);

    void markLastModifiedBy(String auditor);
}
