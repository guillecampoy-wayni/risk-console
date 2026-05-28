package com.example.riskconsole.api;

import com.example.riskconsole.domain.AuditEntry;
import com.example.riskconsole.domain.repository.AuditRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

class AuditControllerTest {

    @Test
    void returnsEmptyListWhenNoAuditEntriesExist() {
        var repo = new FakeEmptyAuditRepository();
        var client = WebTestClient.bindToController(new AuditController(repo)).build();

        client.get()
                .uri("/api/risk/audit")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditEntry.class)
                .hasSize(0);
    }

    @Test
    void returnsAllAuditEntries() {
        var repo = new FakeAuditRepository();
        var client = WebTestClient.bindToController(new AuditController(repo)).build();

        client.get()
                .uri("/api/risk/audit")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditEntry.class)
                .hasSize(2);
    }

    private static class FakeEmptyAuditRepository implements AuditRepository {
        @Override
        public Mono<AuditEntry> save(AuditEntry entry) {
            return Mono.just(entry);
        }

        @Override
        public Flux<AuditEntry> findAll() {
            return Flux.empty();
        }
    }

    private static class FakeAuditRepository implements AuditRepository {
        @Override
        public Mono<AuditEntry> save(AuditEntry entry) {
            return Mono.just(entry);
        }

        @Override
        public Flux<AuditEntry> findAll() {
            return Flux.just(
                    new AuditEntry("1", Instant.now(), "QUERY", "status=BLOCKED"),
                    new AuditEntry("2", Instant.now(), "CSV_EXPORT", "status=ACTIVE")
            );
        }
    }
}
