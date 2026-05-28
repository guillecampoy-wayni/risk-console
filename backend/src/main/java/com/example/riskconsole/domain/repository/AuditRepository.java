package com.example.riskconsole.domain.repository;

import com.example.riskconsole.domain.AuditEntry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AuditRepository {
    Mono<AuditEntry> save(AuditEntry entry);
    Flux<AuditEntry> findAll();
}
