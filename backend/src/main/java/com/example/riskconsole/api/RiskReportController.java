package com.example.riskconsole.api;

import com.example.riskconsole.application.RiskReportService;
import com.example.riskconsole.domain.CustomerRiskReport;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/risk/customers")
public class RiskReportController {
    private static final String CSV_HEADER = "userId,externalId,fullName,email,identification,taxIdentification,userStatus,accountId,country,currency,balance,accountStatus,statusUpdateMotive,statusUpdateComment,statusUpdatedBy,updatedAt";

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

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> exportCsv(
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String internalApiKey,
            @RequestParam(value = "userStatus", defaultValue = "BLOCKED") String userStatus,
            @RequestParam(value = "accountStatus", defaultValue = "ACTIVE,FROZEN,DISABLED,DELETED") String accountStatus,
            @RequestParam(value = "country", defaultValue = "ARG") String country,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "50") int pageSize
    ) {
        return riskReportService.findCustomers(userStatus, accountStatus, country, page, pageSize)
                .collectList()
                .map(rows -> CSV_HEADER + "\n" + rows.stream()
                        .flatMap(RiskReportController::toCsvRows)
                        .collect(Collectors.joining("\n")));
    }

    private static Stream<String> toCsvRows(CustomerRiskReport report) {
        if (report.accounts().isEmpty()) {
            return Stream.of(csvLine(report, null, null, null, null, null, null, null, null, null));
        }
        return report.accounts().stream()
                .map(a -> csvLine(report, a.accountId(), a.country(), a.currency(), a.balance(),
                        a.accountStatus(), a.statusUpdateMotive(), a.statusUpdateComment(),
                        a.statusUpdatedBy(), a.updatedAt()));
    }

    private static String csvLine(CustomerRiskReport r, String accountId, String country,
                                   String currency, String balance, String accountStatus,
                                   String statusUpdateMotive, String statusUpdateComment,
                                   String statusUpdatedBy, String updatedAt) {
        return String.join(",",
                csv(r.userId()), csv(r.externalId()), csv(r.fullName()), csv(r.email()),
                csv(r.identification()), csv(r.taxIdentification()), csv(r.userStatus()),
                csv(accountId), csv(country), csv(currency), csv(balance), csv(accountStatus),
                csv(statusUpdateMotive), csv(statusUpdateComment), csv(statusUpdatedBy), csv(updatedAt)
        );
    }

    private static String csv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
