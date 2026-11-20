package io.zenwave360.example.orderfulfillment;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * HTTP tests for OrderApiController against Postgres and Kafka from Dev Services.
 * Each test places its own order, so tests are independent of execution order.
 */
@QuarkusTest
@TestHTTPEndpoint(OrderApiController.class)
public class OrderApiIntegrationTest extends BaseQuarkusHttpTest {

    @TestHTTPResource
    URI endpoint;

    static final String PLACE_ORDER = """
            {
              "currency": "EUR",
              "items": [ { "productId": "P-1", "productName": "Desk", "quantity": 2, "unitPrice": 100.00 } ]
            }
            """;

    @Test
    void testPlaceOrder_201() throws Exception {
        var response = send("POST", "/orders", PLACE_ORDER);
        assertEquals(201, response.statusCode());
        assertEquals("PLACED", jsonField(response.body(), "status"));
        assertNotNull(jsonField(response.body(), "orderNumber"));
    }

    @Test
    void testPlaceOrder_400() throws Exception {
        var response = send("POST", "/orders", """
                { "currency": "EURO", "items": [] }
                """);
        assertEquals(400, response.statusCode());
    }

    @Test
    void testPayOrder_201() throws Exception {
        var orderNumber = placeOrder();
        var response = send("POST", "/orders/" + orderNumber + "/pay", """
                { "paymentReference": "PAY-1" }
                """);
        assertEquals(201, response.statusCode());
        assertEquals("PAID", jsonField(response.body(), "status"));
    }

    @Test
    void testShipOrder_201() throws Exception {
        var orderNumber = placeOrder();
        send("POST", "/orders/" + orderNumber + "/pay", """
                { "paymentReference": "PAY-1" }
                """);
        var response = send("POST", "/orders/" + orderNumber + "/ship", """
                { "trackingNumber": "TRK-1" }
                """);
        assertEquals(201, response.statusCode());
        assertEquals("SHIPPED", jsonField(response.body(), "status"));
    }

    @Test
    void testShipUnpaidOrder_409() throws Exception {
        var orderNumber = placeOrder();
        var response = send("POST", "/orders/" + orderNumber + "/ship", """
                { "trackingNumber": "TRK-1" }
                """);
        assertEquals(409, response.statusCode());
    }

    @Test
    void testCancelOrder_201() throws Exception {
        var orderNumber = placeOrder();
        var response = send("POST", "/orders/" + orderNumber + "/cancel", null);
        assertEquals(201, response.statusCode());
        assertEquals("CANCELLED", jsonField(response.body(), "status"));
    }

    @Test
    void testGetOrder_200() throws Exception {
        var orderNumber = placeOrder();
        var response = send("GET", "/orders/" + orderNumber, null);
        assertEquals(200, response.statusCode());
        assertEquals(orderNumber, jsonField(response.body(), "orderNumber"));
    }

    @Test
    void testGetOrder_404() throws Exception {
        assertEquals(404, send("GET", "/orders/missing", null).statusCode());
        assertEquals(404, send("POST", "/orders/missing/cancel", null).statusCode());
    }

    private String placeOrder() throws Exception {
        var response = send("POST", "/orders", PLACE_ORDER);
        assertEquals(201, response.statusCode(), response.body());
        return jsonField(response.body(), "orderNumber");
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(resourceUri(endpoint, path))
                .header("Accept", "application/json");
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
