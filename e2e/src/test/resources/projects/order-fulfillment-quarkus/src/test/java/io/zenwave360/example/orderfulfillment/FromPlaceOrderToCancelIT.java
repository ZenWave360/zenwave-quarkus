package io.zenwave360.example.orderfulfillment;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Business flow: placeOrder, getOrder, payOrder, shipOrder, cancelOrder (rejected once shipped).
 */
@QuarkusTest
public class FromPlaceOrderToCancelIT extends BaseQuarkusHttpTest {

    @TestHTTPResource
    URI endpoint;

    @Test
    void testFromPlaceOrderToCancelIT() throws Exception {
        var placed = send("POST", "/orders", """
                { "currency": "EUR", "items": [ { "productId": "P-1", "productName": "Desk", "quantity": 1, "unitPrice": 99.00 } ] }
                """);
        assertEquals(201, placed.statusCode(), placed.body());
        var orderNumber = jsonField(placed.body(), "orderNumber");

        assertEquals(200, send("GET", "/orders/" + orderNumber, null).statusCode());
        assertEquals(201, send("POST", "/orders/" + orderNumber + "/pay", "{ \"paymentReference\": \"PAY-1\" }").statusCode());
        assertEquals(201, send("POST", "/orders/" + orderNumber + "/ship", "{ \"trackingNumber\": \"TRK-1\" }").statusCode());
        assertEquals(409, send("POST", "/orders/" + orderNumber + "/cancel", null).statusCode());
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        var request = HttpRequest.newBuilder().uri(resourceUri(endpoint, path)).header("Accept", "application/json");
        if (body != null) {
            request.header("Content-Type", "application/json");
        }
        request.method(method, body != null ? HttpRequest.BodyPublishers.ofString(body) : HttpRequest.BodyPublishers.noBody());
        return httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String jsonField(String json, String field) {
        var matcher = Pattern.compile("\"" + field + "\"\s*:\s*\"([^\"]*)\"").matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }
}
