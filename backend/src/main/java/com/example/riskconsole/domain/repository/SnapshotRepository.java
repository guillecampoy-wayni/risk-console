package com.example.riskconsole.domain.repository;

import com.example.riskconsole.domain.CustomerRiskReport;
import com.example.riskconsole.domain.Snapshot;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SnapshotRepository {
    Mono<Snapshot> save(List<CustomerRiskReport> data);
    Flux<Snapshot> findAll();
    Mono<Snapshot> findById(String id);
    Mono<List<CustomerRiskReport>> findDataById(String id);
}
