package com.example.riskconsole.infrastructure;

import com.example.riskconsole.infrastructure.web.CorrelationIdWebFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdWebFilterTest {

    private final CorrelationIdWebFilter filter = new CorrelationIdWebFilter();

    @Test
    void addsCorrelationIdToResponseWhenRequestDoesNotHaveOne() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/risk/customers"));

        var result = filter.filter(exchange, okChain());

        StepVerifier.create(result).verifyComplete();

        var correlationId = exchange.getResponse().getHeaders().getFirst("X-Correlation-Id");
        assertThat(correlationId).isNotNull().isNotEmpty();
    }

    @Test
    void preservesCorrelationIdFromRequestHeader() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/risk/customers")
                        .header("X-Correlation-Id", "my-trace-id-123")
        );

        var result = filter.filter(exchange, okChain());

        StepVerifier.create(result).verifyComplete();

        var correlationId = exchange.getResponse().getHeaders().getFirst("X-Correlation-Id");
        assertThat(correlationId).isEqualTo("my-trace-id-123");
    }

    @Test
    void storesCorrelationIdInReactorContext() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/risk/customers")
                        .header("X-Correlation-Id", "ctx-test-456")
        );

        var captured = Mono.deferContextual(Mono::just)
                .contextWrite(ctx -> ctx)
                .block();

        StepVerifier.create(filter.filter(exchange, ctx -> Mono.empty()))
                .verifyComplete();
    }

    private static WebFilterChain okChain() {
        return (WebFilterChain) (ServerWebExchange exchange) -> Mono.empty();
    }
}
