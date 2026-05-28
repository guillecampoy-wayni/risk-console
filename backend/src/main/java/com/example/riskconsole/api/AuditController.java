package com.example.riskconsole.api;

import com.example.riskconsole.domain.AuditEntry;
import com.example.riskconsole.domain.repository.AuditRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/risk")
public class AuditController {

    private final AuditRepository auditRepository;

    public AuditController(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @GetMapping("/audit")
    public Flux<AuditEntry> list() {
        return auditRepository.findAll();
    }
}
