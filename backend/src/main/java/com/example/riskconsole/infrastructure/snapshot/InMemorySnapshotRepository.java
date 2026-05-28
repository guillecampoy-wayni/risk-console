package com.example.riskconsole.infrastructure.snapshot;

import com.example.riskconsole.domain.CustomerRiskReport;
import com.example.riskconsole.domain.Snapshot;
import com.example.riskconsole.domain.repository.SnapshotRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemorySnapshotRepository implements SnapshotRepository {
    private final Map<String, SnapshotEntry> store = new ConcurrentHashMap<>();

    @Override
    public Mono<Snapshot> save(List<CustomerRiskReport> data) {
        var id = UUID.randomUUID().toString();
        var snapshot = new Snapshot(id, Instant.now());
        store.put(id, new SnapshotEntry(snapshot, List.copyOf(data)));
        return Mono.just(snapshot);
    }

    @Override
    public Flux<Snapshot> findAll() {
        return Flux.fromIterable(store.values().stream()
                .map(SnapshotEntry::snapshot)
                .toList());
    }

    @Override
    public Mono<Snapshot> findById(String id) {
        var entry = store.get(id);
        return entry != null ? Mono.just(entry.snapshot()) : Mono.empty();
    }

    @Override
    public Mono<List<CustomerRiskReport>> findDataById(String id) {
        var entry = store.get(id);
        return entry != null ? Mono.just(entry.data()) : Mono.empty();
    }

    private record SnapshotEntry(Snapshot snapshot, List<CustomerRiskReport> data) {}
}
