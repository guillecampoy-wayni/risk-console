package com.example.riskconsole.infrastructure.web;

import com.example.riskconsole.domain.AuditEntry;
import com.example.riskconsole.domain.repository.AuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuditWebFilterTest {

    private final FakeAuditRepository auditRepo = new FakeAuditRepository();
    private final AuditWebFilter filter = new AuditWebFilter(auditRepo);

    @Test
    void capturesQueryOnCustomerGetRequest() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/risk/customers?userStatus=BLOCKED&country=ARG")
        );

        var result = filter.filter(exchange, okChain());

        StepVerifier.create(result).verifyComplete();
        assertThat(auditRepo.entries).hasSize(1);
        var entry = auditRepo.entries.getFirst();
        assertThat(entry.action()).isEqualTo("QUERY");
        assertThat(entry.details()).contains("userStatus=BLOCKED");
    }

    @Test
    void capturesCsvExportWhenAcceptHeaderIsTextPlain() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/risk/customers?userStatus=ACTIVE")
                        .header("Accept", "text/plain")
        );

        var result = filter.filter(exchange, okChain());

        StepVerifier.create(result).verifyComplete();
        assertThat(auditRepo.entries).hasSize(1);
        assertThat(auditRepo.entries.getFirst().action()).isEqualTo("CSV_EXPORT");
    }

    @Test
    void capturesSnapshotCreationOnPostRequest() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/risk/snapshots")
        );

        var result = filter.filter(exchange, okChain());

        StepVerifier.create(result).verifyComplete();
        assertThat(auditRepo.entries).hasSize(1);
        assertThat(auditRepo.entries.getFirst().action()).isEqualTo("SNAPSHOT_TAKE");
    }

    @Test
    void capturesSnapshotListOnGetRequest() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/risk/snapshots")
        );

        var result = filter.filter(exchange, okChain());

        StepVerifier.create(result).verifyComplete();
        assertThat(auditRepo.entries).hasSize(1);
        assertThat(auditRepo.entries.getFirst().action()).isEqualTo("SNAPSHOT_LIST");
    }

    @Test
    void doesNotCaptureAuditEndpoint() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/risk/audit")
        );

        var result = filter.filter(exchange, okChain());

        StepVerifier.create(result).verifyComplete();
        assertThat(auditRepo.entries).isEmpty();
    }

    @Test
    void doesNotCaptureNonAuditableEndpoints() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health")
        );

        var result = filter.filter(exchange, okChain());

        StepVerifier.create(result).verifyComplete();
        assertThat(auditRepo.entries).isEmpty();
    }

    private static WebFilterChain okChain() {
        return (WebFilterChain) (ServerWebExchange exchange) -> Mono.empty();
    }

    private static class FakeAuditRepository implements AuditRepository {
        final List<AuditEntry> entries = new ArrayList<>();

        @Override
        public Mono<AuditEntry> save(AuditEntry entry) {
            entries.add(entry);
            return Mono.just(entry);
        }

        @Override
        public Flux<AuditEntry> findAll() {
            return Flux.fromIterable(entries);
        }
    }
}
