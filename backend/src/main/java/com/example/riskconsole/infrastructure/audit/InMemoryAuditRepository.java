package com.example.riskconsole.infrastructure.audit;

import com.example.riskconsole.domain.AuditEntry;
import com.example.riskconsole.domain.repository.AuditRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryAuditRepository implements AuditRepository {
    private final Map<String, AuditEntry> store = new ConcurrentHashMap<>();

    @Override
    public Mono<AuditEntry> save(AuditEntry entry) {
        store.put(entry.id(), entry);
        return Mono.just(entry);
    }

    @Override
    public Flux<AuditEntry> findAll() {
        var sorted = store.values().stream()
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                .toList();
        return Flux.fromIterable(sorted);
    }
}
