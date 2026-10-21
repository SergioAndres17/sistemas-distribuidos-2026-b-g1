package co.edu.corhuila.synkro;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.http.*;
import org.springframework.test.context.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureObservability
class WorkflowWorkshopTest {
    static HttpServer customers;
    static AtomicReference<String> trace = new AtomicReference<>(), correlation = new AtomicReference<>();
    static AtomicInteger status = new AtomicInteger(200);
    @Autowired TestRestTemplate http;
    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry properties) throws Exception {
        customers = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        customers.createContext("/api/v1/customers/22222222-2222-4222-8222-222222222222", exchange -> {
            trace.set(exchange.getRequestHeaders().getFirst("traceparent"));
            correlation.set(exchange.getRequestHeaders().getFirst("X-Correlation-Id"));
            exchange.sendResponseHeaders(status.get(), -1);
            exchange.close();
        });
        customers.start();
        properties.add("customers.base-url", () -> "http://127.0.0.1:" + customers.getAddress().getPort());
    }
    @AfterAll static void stop() { customers.stop(0); }
    ResponseEntity<String> send() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("traceparent", "00-1234567890abcdef1234567890abcdef-1234567890abcdef-01");
        headers.set("X-Correlation-Id", "workshop-sale-001");
        return http.postForEntity("/api/v1/sagas/register-sale", new HttpEntity<>(
            "{\"customerId\":\"22222222-2222-4222-8222-222222222222\"}", headers), String.class);
    }
    @Test void preservesTraceAcrossHttpAndExposesMetrics() {
        var response = send();
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(trace.get()).matches("00-1234567890abcdef1234567890abcdef-[0-9a-f]{16}-01");
        assertThat(trace.get()).isNotEqualTo(response.getHeaders().getFirst("traceparent"));
        assertThat(correlation.get()).isEqualTo("workshop-sale-001");
        await().untilAsserted(() -> assertThat(http.getForObject("/metrics", String.class))
            .contains("workflow_requests_total{outcome=\"success\"} 1.0")
            .contains("workflow_request_duration_seconds_bucket{")
            .contains("workflow_request_duration_seconds_count{outcome=\"success\"} 1"));
    }
    @Test void countsDownstreamFailure() {
        status.set(503);
        try {
            assertThat(send().getStatusCode().value()).isEqualTo(502);
            await().untilAsserted(() -> assertThat(http.getForObject("/metrics", String.class))
                .contains("workflow_requests_total{outcome=\"error\"} 1.0"));
        } finally { status.set(200); }
    }
}
