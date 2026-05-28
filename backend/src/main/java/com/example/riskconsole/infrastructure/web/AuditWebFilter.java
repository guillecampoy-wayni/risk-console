package com.example.riskconsole.infrastructure.web;

import com.example.riskconsole.domain.AuditEntry;
import com.example.riskconsole.domain.repository.AuditRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Component
public class AuditWebFilter implements WebFilter {

    private static final Set<String> CUSTOMER_PATHS = Set.of("/api/risk/customers");
    private static final Set<String> SNAPSHOT_PATHS = Set.of("/api/risk/snapshots");

    private final AuditRepository auditRepository;

    public AuditWebFilter(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var path = exchange.getRequest().getURI().getPath();
        var action = resolveAction(path, exchange);

        if (action == null) {
            return chain.filter(exchange);
        }

        var details = exchange.getRequest().getURI().getRawQuery() != null
                ? exchange.getRequest().getURI().getRawQuery()
                : "";

        return chain.filter(exchange).then(
                auditRepository.save(new AuditEntry(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        action,
                        details
                )).then()
        );
    }

    private static String resolveAction(String path, ServerWebExchange exchange) {
        if (CUSTOMER_PATHS.contains(path)) {
            var accept = exchange.getRequest().getHeaders().getFirst("Accept");
            if (accept != null && accept.equals("text/plain")) {
                return "CSV_EXPORT";
            }
            return "QUERY";
        }
        if (SNAPSHOT_PATHS.contains(path)) {
            var method = exchange.getRequest().getMethod();
            if (method != null && "POST".equalsIgnoreCase(method.name())) {
                return "SNAPSHOT_TAKE";
            }
            return "SNAPSHOT_LIST";
        }
        return null;
    }
}
