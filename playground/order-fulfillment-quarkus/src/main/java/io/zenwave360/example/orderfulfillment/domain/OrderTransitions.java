package io.zenwave360.example.orderfulfillment.domain;

import java.util.Arrays;
import java.util.List;

public final class OrderTransitions {

    private OrderTransitions() {}

    public static void ensureCanPayOrder(Order entity) {
        require("payOrder", entity, OrderStatus.PLACED);
    }

    public static void ensureCanShipOrder(Order entity) {
        require("shipOrder", entity, OrderStatus.PAID);
    }

    public static void ensureCanCancelOrder(Order entity) {
        require("cancelOrder", entity, OrderStatus.PLACED, OrderStatus.PAID);
    }

    private static void require(String command, Order entity, OrderStatus... allowed) {
        var current = entity.getStatus();
        if (current != null) {
            for (var candidate : allowed) {
                if (candidate == current) {
                    return;
                }
            }
        }
        throw new InvalidStateTransitionException(
                entity.getClass().getSimpleName(),
                entity.getId(),
                "orderNumber=" + entity.getOrderNumber(),
                command,
                current,
                Arrays.asList(allowed));
    }

    public static class InvalidStateTransitionException extends IllegalStateException {
        private final String entityType;
        private final Object entityId;
        private final Object naturalId;
        private final String operation;
        private final Object currentState;
        private final List<?> allowedStates;

        public InvalidStateTransitionException(
                String entityType,
                Object entityId,
                Object naturalId,
                String operation,
                Object currentState,
                List<?> allowedStates) {
            super("Invalid state transition for " + entityType + " ("
                    + (naturalId != null ? naturalId : "id=" + entityId) + "): " + "operation '" + operation
                    + "' not allowed from state " + currentState + ", allowed states: " + allowedStates);
            this.entityType = entityType;
            this.entityId = entityId;
            this.naturalId = naturalId;
            this.operation = operation;
            this.currentState = currentState;
            this.allowedStates = allowedStates;
        }

        public String getEntityType() {
            return entityType;
        }

        public Object getEntityId() {
            return entityId;
        }

        public Object getNaturalId() {
            return naturalId;
        }

        public String getOperation() {
            return operation;
        }

        public Object getCurrentState() {
            return currentState;
        }

        public List<?> getAllowedStates() {
            return allowedStates;
        }
    }
}
