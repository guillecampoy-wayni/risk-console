package com.example.riskconsole.infrastructure.audit;

import com.example.riskconsole.domain.AuditEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryAuditRepositoryTest {

    private InMemoryAuditRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAuditRepository();
    }

    @Test
    void savesEntryAndAssignsId() {
        var entry = new AuditEntry("custom-id", Instant.now(), "QUERY", "/api/risk/customers");

        StepVerifier.create(repository.save(entry))
                .assertNext(saved -> {
                    assertThat(saved.id()).isEqualTo("custom-id");
                    assertThat(saved.createdAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void returnsEmptyListWhenNoEntriesExist() {
        StepVerifier.create(repository.findAll())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void listsAllSavedEntries() {
        repository.save(new AuditEntry("a", Instant.now(), "QUERY", "?status=BLOCKED")).block();
        repository.save(new AuditEntry("b", Instant.now(), "CSV_EXPORT", "?status=ACTIVE")).block();

        StepVerifier.create(repository.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void returnsEntriesInReverseChronologicalOrder() {
        var earlier = Instant.parse("2026-05-28T10:00:00Z");
        var later = Instant.parse("2026-05-28T11:00:00Z");

        repository.save(new AuditEntry("1", earlier, "QUERY", "first")).block();
        repository.save(new AuditEntry("2", later, "QUERY", "second")).block();

        StepVerifier.create(repository.findAll())
                .assertNext(e -> assertThat(e.id()).isEqualTo("2"))
                .assertNext(e -> assertThat(e.id()).isEqualTo("1"))
                .verifyComplete();
    }
}
