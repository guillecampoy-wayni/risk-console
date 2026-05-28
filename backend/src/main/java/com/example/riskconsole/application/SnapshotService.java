package com.example.riskconsole.application;

import com.example.riskconsole.domain.CustomerRiskReport;
import com.example.riskconsole.domain.Snapshot;
import com.example.riskconsole.domain.repository.SnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class SnapshotService {
    private final SnapshotRepository snapshotRepository;
    private final RiskReportService riskReportService;

    @Autowired
    public SnapshotService(SnapshotRepository snapshotRepository, RiskReportService riskReportService) {
        this.snapshotRepository = snapshotRepository;
        this.riskReportService = riskReportService;
    }

    public SnapshotService(SnapshotRepository snapshotRepository) {
        this(snapshotRepository, null);
    }

    public Mono<Snapshot> takeSnapshot() {
        return riskReportService.findCustomers("BLOCKED", "ACTIVE,FROZEN,DISABLED,DELETED", "ARG", 1, 50)
                .collectList()
                .flatMap(snapshotRepository::save);
    }

    public Flux<Snapshot> listSnapshots() {
        return snapshotRepository.findAll();
    }
}
