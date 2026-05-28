package com.example.riskconsole.application;

import com.example.riskconsole.domain.AccountRiskDetail;
import com.example.riskconsole.domain.CustomerRiskReport;
import com.example.riskconsole.domain.Snapshot;
import com.example.riskconsole.domain.repository.SnapshotRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SnapshotServiceTest {

    private final FakeRiskReportService reportService = new FakeRiskReportService();
    private final FakeSnapshotRepository repository = new FakeSnapshotRepository();
    private final SnapshotService service = new SnapshotService(repository, reportService);

    @Test
    void takeSnapshotFetchesDataAndSavesToRepository() {
        StepVerifier.create(service.takeSnapshot())
                .assertNext(snapshot -> {
                    assertThat(snapshot.id()).isNotNull();
                    assertThat(snapshot.createdAt()).isNotNull();
                    assertThat(repository.savedData).hasSize(1);
                    var firstEntry = repository.savedData.values().iterator().next();
                    assertThat(firstEntry.getFirst().userId()).isEqualTo("usr-1");
                })
                .verifyComplete();
    }

    @Test
    void listSnapshotsReturnsAllSavedSnapshots() {
        service.takeSnapshot().block();
        service.takeSnapshot().block();

        StepVerifier.create(service.listSnapshots())
                .expectNextCount(2)
                .verifyComplete();
    }

    private static class FakeRiskReportService extends RiskReportService {
        FakeRiskReportService() {
            super(null);
        }

        @Override
        public Flux<CustomerRiskReport> findCustomers(String userStatus, String accountStatus, String country, int page, int pageSize) {
            var account = new AccountRiskDetail("acc-1", "ARG", "ARS", "100.00",
                    "FROZEN", "OTHER", "review", "CLIENT", "2026-05-28T12:00:00Z");
            var report = new CustomerRiskReport("usr-1", "ext-1", "Juan Perez",
                    "juan@test.com", "DNI 1", "CUIL 2", "BLOCKED", List.of(account));
            return Flux.just(report);
        }
    }

    private static class FakeSnapshotRepository implements SnapshotRepository {
        private final java.util.Map<String, List<CustomerRiskReport>> savedData = new java.util.concurrent.ConcurrentHashMap<>();

        @Override
        public Mono<Snapshot> save(List<CustomerRiskReport> data) {
            var id = "snap-" + savedData.size();
            var snapshot = new Snapshot(id, Instant.now());
            savedData.put(id, data);
            return Mono.just(snapshot);
        }

        @Override
        public Flux<Snapshot> findAll() {
            return Flux.fromIterable(savedData.keySet().stream()
                    .map(id -> new Snapshot(id, Instant.now()))
                    .toList());
        }

        @Override
        public Mono<Snapshot> findById(String id) {
            return savedData.containsKey(id)
                    ? Mono.just(new Snapshot(id, Instant.now()))
                    : Mono.empty();
        }

        @Override
        public Mono<List<CustomerRiskReport>> findDataById(String id) {
            var data = savedData.get(id);
            return data != null ? Mono.just(data) : Mono.empty();
        }
    }
}
