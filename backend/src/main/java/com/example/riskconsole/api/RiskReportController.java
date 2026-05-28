package com.example.riskconsole.api;

import com.example.riskconsole.application.RiskReportService;
import com.example.riskconsole.domain.CustomerRiskReport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/risk/customers")
public class RiskReportController {
    private final RiskReportService riskReportService;

    public RiskReportController(RiskReportService riskReportService) {
        this.riskReportService = riskReportService;
    }

    @GetMapping
    public Flux<CustomerRiskReport> search(
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String internalApiKey,
            @RequestParam(value = "userStatus", defaultValue = "BLOCKED") String userStatus,
            @RequestParam(value = "accountStatus", defaultValue = "ACTIVE,FROZEN,DISABLED,DELETED") String accountStatus,
            @RequestParam(value = "country", defaultValue = "ARG") String country,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "50") int pageSize
    ) {
        return riskReportService.findCustomers(userStatus, accountStatus, country, page, pageSize);
    }
}
