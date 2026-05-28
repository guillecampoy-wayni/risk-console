package com.example.riskconsole.infrastructure.web;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdWebFilter implements WebFilter {

    static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    static final String CORRELATION_ID_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var header = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        var correlationId = (header != null && !header.isBlank()) ? header : UUID.randomUUID().toString();

        exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);

        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put(CORRELATION_ID_KEY, correlationId));
    }
}
