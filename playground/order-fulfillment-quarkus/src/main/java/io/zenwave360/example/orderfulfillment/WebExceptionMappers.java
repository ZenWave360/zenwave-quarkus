package io.zenwave360.example.orderfulfillment;

import io.zenwave360.example.orderfulfillment.domain.OrderTransitions.InvalidStateTransitionException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import java.util.NoSuchElementException;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/**
 * Maps domain exceptions to HTTP problem responses (hand-written, not generated).
 */
public class WebExceptionMappers {

    @ServerExceptionMapper
    public Response notFound(NoSuchElementException e) {
        return problem(Response.Status.NOT_FOUND, e.getMessage());
    }

    @ServerExceptionMapper
    public Response invalidTransition(InvalidStateTransitionException e) {
        return problem(Response.Status.CONFLICT, e.getMessage());
    }

    private static Response problem(Response.Status status, String detail) {
        return Response.status(status)
                .type("application/problem+json")
                .entity(Map.of("title", status.getReasonPhrase(), "status", status.getStatusCode(), "detail", String.valueOf(detail)))
                .build();
    }
}
