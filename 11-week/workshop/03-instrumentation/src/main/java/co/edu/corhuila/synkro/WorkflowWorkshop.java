package co.edu.corhuila.synkro;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.OncePerRequestFilter;

@SpringBootApplication
@RestController
public class WorkflowWorkshop {
    public static void main(String[] args) {
        SpringApplication.run(WorkflowWorkshop.class, args);
    }

    record Trace(String traceId, String spanId, String flags, String correlationId, String parentSpanId) {
        String header() { return "00-" + traceId + "-" + spanId + "-" + flags; }
    }

    static String randomId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    static Trace readTrace(HttpServletRequest request) {
        String incoming = request.getHeader("traceparent");
        String traceId = randomId();
        String flags = "00";
        String parent = null;
        if (incoming != null && incoming.matches("00-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}")) {
            String[] parts = incoming.split("-");
            if (!parts[1].equals("0".repeat(32)) && !parts[2].equals("0".repeat(16))) {
                traceId = parts[1];
                parent = parts[2];
                flags = (Integer.parseInt(parts[3], 16) & 1) == 1 ? "01" : "00";
            }
        }
        String correlation = request.getHeader("X-Correlation-Id");
        if (correlation == null || !correlation.matches("[A-Za-z0-9._-]{1,64}")) {
            correlation = UUID.randomUUID().toString();
        }
        return new Trace(traceId, randomId().substring(0, 16), flags, correlation, parent);
    }

    static void log(ObjectMapper mapper, Trace trace, String level,
                    String event, String message, long elapsed) {
        var entry = mapper.createObjectNode();
        entry.put("timestamp", Instant.now().toString());
        entry.put("level", level).put("service", "synkro-workflow");
        entry.put("version", "workshop-cp3").put("environment", "workshop");
        entry.put("trace_id", trace.traceId()).put("span_id", trace.spanId());
        entry.put("correlation_id", trace.correlationId()).put("event", event);
        entry.put("message", message).put("duration_ms", elapsed);
        if (trace.parentSpanId() != null) entry.put("parent_span_id", trace.parentSpanId());
        System.out.println(entry);
    }

    @Bean
    OncePerRequestFilter traceFilter(ObjectMapper mapper, MeterRegistry meters) {
        return new OncePerRequestFilter() {
            @Override
            protected boolean shouldNotFilter(HttpServletRequest request) {
                return !request.getRequestURI().equals("/api/v1/sagas/register-sale");
            }
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                    HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                Trace trace = readTrace(request);
                request.setAttribute("trace", trace);
                response.setHeader("traceparent", trace.header());
                response.setHeader("X-Correlation-Id", trace.correlationId());
                long started = System.nanoTime();
                boolean failed = false;
                log(mapper, trace, "INFO", "request.started", "Request started", 0);
                try {
                    chain.doFilter(request, response);
                } catch (IOException | ServletException | RuntimeException error) {
                    failed = true;
                    throw error;
                } finally {
                    failed = failed || response.getStatus() >= 500;
                    String outcome = failed ? "error" : response.getStatus() >= 400 ? "rejected" : "success";
                    meters.counter("workflow.requests", "outcome", outcome).increment();
                    Timer.builder("workflow.request.duration").tag("outcome", outcome)
                        .serviceLevelObjectives(Duration.ofMillis(100), Duration.ofMillis(300),
                            Duration.ofSeconds(1), Duration.ofSeconds(3))
                        .register(meters).record(System.nanoTime() - started, TimeUnit.NANOSECONDS);
                    log(mapper, trace, failed ? "ERROR" : "INFO", "request.completed",
                        failed ? "Request failed" : "Request completed",
                        (System.nanoTime() - started) / 1_000_000);
                }
            }
        };
    }

    @Value("${customers.base-url}")
    private String customersUrl;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    record SaleRequest(UUID customerId) {}

    @PostMapping("/api/v1/sagas/register-sale")
    Map<String, String> registerSale(@RequestBody SaleRequest body,
            HttpServletRequest request) {
        if (body.customerId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        Trace server = (Trace) request.getAttribute("trace");
        Trace child = new Trace(server.traceId(), randomId().substring(0, 16),
            server.flags(), server.correlationId(), server.spanId());
        var outgoing = HttpRequest.newBuilder(URI.create(customersUrl + "/api/v1/customers/" + body.customerId()))
            .timeout(Duration.ofSeconds(3)).header("traceparent", child.header())
            .header("X-Correlation-Id", child.correlationId()).GET().build();
        long started = System.nanoTime();
        boolean success = false;
        try {
            var result = client.send(outgoing, HttpResponse.BodyHandlers.discarding());
            if (result.statusCode() != 200) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
            success = true;
            return Map.of("message", "Customer lookup completed; no sale was created");
        } catch (IOException error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        } finally {
            log(new ObjectMapper(), child, success ? "INFO" : "ERROR", "customer.lookup.completed",
                success ? "Customer lookup completed" : "Customer lookup failed",
                (System.nanoTime() - started) / 1_000_000);
        }
    }
}
