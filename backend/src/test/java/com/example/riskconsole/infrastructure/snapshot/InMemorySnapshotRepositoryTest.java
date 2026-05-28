package com.example.riskconsole.infrastructure.snapshot;

import com.example.riskconsole.domain.AccountRiskDetail;
import com.example.riskconsole.domain.CustomerRiskReport;
import com.example.riskconsole.domain.Snapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemorySnapshotRepositoryTest {

    private InMemorySnapshotRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemorySnapshotRepository();
    }

    @Test
    void savesSnapshotAndAssignsId() {
        var data = List.of(report("usr-1"));

        StepVerifier.create(repository.save(data))
                .assertNext(snapshot -> {
                    assertThat(snapshot.id()).isNotNull().isNotEmpty();
                    assertThat(snapshot.createdAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void returnsEmptyListWhenNoSnapshotsExist() {
        StepVerifier.create(repository.findAll())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void listsAllSavedSnapshots() {
        repository.save(List.of(report("u1"))).block();
        repository.save(List.of(report("u2"))).block();
        repository.save(List.of(report("u3"))).block();

        StepVerifier.create(repository.findAll())
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findsSnapshotById() {
        var saved = repository.save(List.of(report("usr-1"))).block();

        StepVerifier.create(repository.findById(saved.id()))
                .assertNext(found -> assertThat(found.id()).isEqualTo(saved.id()))
                .verifyComplete();
    }

    @Test
    void returnsEmptyWhenSnapshotNotFound() {
        StepVerifier.create(repository.findById("nonexistent"))
                .verifyComplete();
    }

    @Test
    void retrievesDataBySnapshotId() {
        var data = List.of(report("usr-1"), report("usr-2"));
        var saved = repository.save(data).block();

        StepVerifier.create(repository.findDataById(saved.id()))
                .assertNext(found -> {
                    assertThat(found).hasSize(2);
                    assertThat(found.getFirst().userId()).isEqualTo("usr-1");
                    assertThat(found.getLast().userId()).isEqualTo("usr-2");
                })
                .verifyComplete();
    }

    private static CustomerRiskReport report(String userId) {
        return new CustomerRiskReport(userId, "ext-" + userId, "User " + userId,
                userId + "@test.com", "DNI 1", "CUIL 2", "BLOCKED", List.of(
                new AccountRiskDetail("acc-" + userId, "ARG", "ARS", "100.00",
                        "FROZEN", "OTHER", "review", "CLIENT", "2026-05-28T12:00:00Z")
        ));
    }
}
