package com.example.riskconsole.application;

import com.example.riskconsole.domain.AccountRiskDetail;
import com.example.riskconsole.domain.CustomerRiskReport;
import com.example.riskconsole.infrastructure.pomelo.PomeloAccountDto;
import com.example.riskconsole.infrastructure.pomelo.PomeloGateway;
import com.example.riskconsole.infrastructure.pomelo.PomeloUserDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class RiskReportService {
    private final PomeloGateway pomeloGateway;

    public RiskReportService(PomeloGateway pomeloGateway) {
        this.pomeloGateway = pomeloGateway;
    }

    public Flux<CustomerRiskReport> findCustomers(String userStatus, String accountStatus, String country, int page, int pageSize) {
        return pomeloGateway.searchUsers(userStatus, page, pageSize)
                .flatMapMany(response -> Flux.fromIterable(response.data()))
                .flatMap(user -> buildReport(user, country, accountStatus), 8);
    }

    private Mono<CustomerRiskReport> buildReport(PomeloUserDto user, String country, String accountStatus) {
        return pomeloGateway.listAccounts(country, user.id(), accountStatus)
                .map(response -> toReport(user, response.data()));
    }

    private CustomerRiskReport toReport(PomeloUserDto user, List<PomeloAccountDto> accounts) {
        return new CustomerRiskReport(
                user.id(),
                user.external_id(),
                joinName(user.name(), user.surname()),
                user.email(),
                joinIdentification(user.identification_type(), user.identification_value()),
                joinIdentification(user.tax_identification_type(), user.tax_identification_value()),
                user.status(),
                accounts.stream().map(this::toAccountRiskDetail).toList()
        );
    }

    private AccountRiskDetail toAccountRiskDetail(PomeloAccountDto account) {
        return new AccountRiskDetail(
                account.id(),
                account.country(),
                account.currency(),
                account.balance(),
                account.status(),
                account.status_update_motive(),
                account.status_update_comment(),
                account.status_updated_by(),
                account.updated_at()
        );
    }

    private String joinName(String name, String surname) {
        return String.join(" ", List.of(nullToEmpty(name), nullToEmpty(surname))).trim();
    }

    private String joinIdentification(String type, String value) {
        return String.join(" ", List.of(nullToEmpty(type), nullToEmpty(value))).trim();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
