package io.zenwave360.example.orderfulfillment.domain;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * JPA listener for {@code @auditing} entities: fills {@code createdBy} and {@code lastModifiedBy}.
 * Dates are set by Hibernate {@code @CreationTimestamp} / {@code @UpdateTimestamp}.
 *
 * <p>Quarkus creates entity listeners as CDI beans, so an {@link AuditorProvider} bean is injected
 * (and kept by ArC's unused-bean removal). Without one, or outside CDI, the auditor is {@value #DEFAULT_AUDITOR}.
 */
public class AuditingEntityListener {

    public static final String DEFAULT_AUDITOR = "system";

    @Inject
    Instance<AuditorProvider> auditorProvider;

    @PrePersist
    public void onPrePersist(Object entity) {
        if (entity instanceof Auditable auditable) {
            var auditor = currentAuditor();
            auditable.markCreatedBy(auditor);
            auditable.markLastModifiedBy(auditor);
        }
    }

    @PreUpdate
    public void onPreUpdate(Object entity) {
        if (entity instanceof Auditable auditable) {
            auditable.markLastModifiedBy(currentAuditor());
        }
    }

    protected String currentAuditor() {
        if (auditorProvider != null && auditorProvider.isResolvable()) {
            var auditor = auditorProvider.get().currentAuditor();
            return auditor != null ? auditor : DEFAULT_AUDITOR;
        }
        return DEFAULT_AUDITOR;
    }
}
