package com.example.riskconsole.application;

import com.example.riskconsole.infrastructure.pomelo.PomeloAccountDto;
import com.example.riskconsole.infrastructure.pomelo.PomeloDataGateway;
import com.example.riskconsole.infrastructure.pomelo.PomeloPageResponse;
import com.example.riskconsole.infrastructure.pomelo.PomeloUserDto;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

class RiskReportServiceTest {
    private final FakePomeloGateway pomeloGateway = new FakePomeloGateway();
    private final RiskReportService service = new RiskReportService(pomeloGateway);

    @Test
    void returnsBlockedCustomersWithAssociatedAccountRiskDetails() {
        var user = new PomeloUserDto(
                "usr-1",
                "Juan",
                "Perez",
                "juan@example.com",
                "DNI",
                "12345678",
                "CUIL",
                "20123456789",
                "client-1",
                "BLOCKED"
        );
        var account = new PomeloAccountDto(
                "acc-1",
                "ARG",
                "982345.12",
                "FROZEN",
                "ARS",
                "OTHER",
                "Analyst review",
                "CLIENT",
                "2024-01-01T00:00:00Z"
        );

        pomeloGateway.usersResponse = page(List.of(user));
        pomeloGateway.accountsResponse = page(List.of(account));

        StepVerifier.create(service.findCustomers("BLOCKED", "ACTIVE,FROZEN", "ARG", 1, 50))
                .assertNext(report -> {
                    org.assertj.core.api.Assertions.assertThat(report.userId()).isEqualTo("usr-1");
                    org.assertj.core.api.Assertions.assertThat(report.externalId()).isEqualTo("client-1");
                    org.assertj.core.api.Assertions.assertThat(report.fullName()).isEqualTo("Juan Perez");
                    org.assertj.core.api.Assertions.assertThat(report.identification()).isEqualTo("DNI 12345678");
                    org.assertj.core.api.Assertions.assertThat(report.taxIdentification()).isEqualTo("CUIL 20123456789");
                    org.assertj.core.api.Assertions.assertThat(report.userStatus()).isEqualTo("BLOCKED");
                    org.assertj.core.api.Assertions.assertThat(report.accounts()).hasSize(1);
                    org.assertj.core.api.Assertions.assertThat(report.accounts().getFirst().accountId()).isEqualTo("acc-1");
                    org.assertj.core.api.Assertions.assertThat(report.accounts().getFirst().accountStatus()).isEqualTo("FROZEN");
                    org.assertj.core.api.Assertions.assertThat(report.accounts().getFirst().statusUpdateComment()).isEqualTo("Analyst review");
                })
                .verifyComplete();
    }

    @Test
    void keepsIdentityFieldsReadableWhenPomeloReturnsPartialNames() {
        var user = new PomeloUserDto(
                "usr-2",
                null,
                "Gomez",
                "ana@example.com",
                "DNI",
                null,
                null,
                "27999999999",
                "client-2",
                "BLOCKED"
        );

        pomeloGateway.usersResponse = page(List.of(user));
        pomeloGateway.accountsResponse = page(List.of());

        StepVerifier.create(service.findCustomers("BLOCKED", "ACTIVE", "ARG", 2, 10))
                .assertNext(report -> {
                    org.assertj.core.api.Assertions.assertThat(report.fullName()).isEqualTo("Gomez");
                    org.assertj.core.api.Assertions.assertThat(report.identification()).isEqualTo("DNI");
                    org.assertj.core.api.Assertions.assertThat(report.taxIdentification()).isEqualTo("27999999999");
                    org.assertj.core.api.Assertions.assertThat(report.accounts()).isEmpty();
                })
                .verifyComplete();
    }

    private static <T> PomeloPageResponse<T> page(List<T> data) {
        return new PomeloPageResponse<>(
                data,
                new PomeloPageResponse.Meta(new PomeloPageResponse.Pagination(1, 1, data.size()))
        );
    }

    private static class FakePomeloGateway implements PomeloDataGateway {
        private PomeloPageResponse<PomeloUserDto> usersResponse;
        private PomeloPageResponse<PomeloAccountDto> accountsResponse;

        @Override
        public Mono<PomeloPageResponse<PomeloUserDto>> searchUsers(String userStatus, int page, int pageSize) {
            return Mono.just(usersResponse);
        }

        @Override
        public Mono<PomeloPageResponse<PomeloAccountDto>> listAccounts(String country, String userId, String accountStatusCsv) {
            return Mono.just(accountsResponse);
        }
    }
}
