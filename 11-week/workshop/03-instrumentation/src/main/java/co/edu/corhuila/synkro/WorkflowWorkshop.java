package co.edu.corhuila.synkro;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
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

    record Trace(String traceId, String spanId, String flags, String correlationId) {
        String header() { return "00-" + traceId + "-" + spanId + "-" + flags; }
    }

    static String randomId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    static Trace readTrace(HttpServletRequest request) {
        String incoming = request.getHeader("traceparent");
        String traceId = randomId();
        String flags = "00";
        if (incoming != null && incoming.matches("00-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}")) {
            String[] parts = incoming.split("-");
            if (!parts[1].equals("0".repeat(32)) && !parts[2].equals("0".repeat(16))) {
                traceId = parts[1];
                flags = (Integer.parseInt(parts[3], 16) & 1) == 1 ? "01" : "00";
            }
        }
        String correlation = request.getHeader("X-Correlation-Id");
        if (correlation == null || !correlation.matches("[A-Za-z0-9._-]{1,64}")) {
            correlation = UUID.randomUUID().toString();
        }
        return new Trace(traceId, randomId().substring(0, 16), flags, correlation);
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
        System.out.println(entry);
    }

    @Bean
    OncePerRequestFilter traceFilter(ObjectMapper mapper) {
        return new OncePerRequestFilter() {
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
                    log(mapper, trace, failed ? "ERROR" : "INFO", "request.completed",
                        failed ? "Request failed" : "Request completed",
                        (System.nanoTime() - started) / 1_000_000);
                }
            }
        };
    }

    @PostMapping("/api/v1/sagas/register-sale")
    Map<String, String> registerSale() {
        return Map.of("message", "Trace filter checked; no sale was created");
    }
}
