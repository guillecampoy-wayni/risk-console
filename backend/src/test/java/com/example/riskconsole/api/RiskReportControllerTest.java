package com.example.riskconsole.api;

import com.example.riskconsole.application.RiskReportService;
import com.example.riskconsole.domain.CustomerRiskReport;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

class RiskReportControllerTest {
    @Test
    void delegatesSearchUsingRequestFilters() {
        var expected = new CustomerRiskReport(
                "usr-1",
                "client-1",
                "Juan Perez",
                "juan@example.com",
                "DNI 12345678",
                "CUIL 20123456789",
                "BLOCKED",
                List.of()
        );
        var service = new FakeRiskReportService(Flux.just(expected));

        var controller = new RiskReportController(service);

        StepVerifier.create(controller.search("dev-local-key", "BLOCKED", "ACTIVE", "ARG", 1, 50))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void bindsHttpRequestFiltersWithoutRequiringCompilerParameterMetadata() {
        var expected = new CustomerRiskReport(
                "usr-1",
                "client-1",
                "Juan Perez",
                "juan@example.com",
                "DNI 12345678",
                "CUIL 20123456789",
                "BLOCKED",
                List.of()
        );
        var service = new FakeRiskReportService(Flux.just(expected)).expectingAccountStatus("FROZEN,DISABLED");
        var client = WebTestClient.bindToController(new RiskReportController(service)).build();

        client.get()
                .uri("/api/risk/customers?userStatus=BLOCKED&accountStatus=FROZEN,DISABLED&country=ARG&page=1&pageSize=50")
                .header("X-Internal-Api-Key", "dev-local-key")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerRiskReport.class)
                .hasSize(1)
                .contains(expected);
    }

    private static class FakeRiskReportService extends RiskReportService {
        private final Flux<CustomerRiskReport> response;
        private String expectedAccountStatus = "ACTIVE";

        private FakeRiskReportService(Flux<CustomerRiskReport> response) {
            super(null);
            this.response = response;
        }

        private FakeRiskReportService expectingAccountStatus(String expectedAccountStatus) {
            this.expectedAccountStatus = expectedAccountStatus;
            return this;
        }

        @Override
        public Flux<CustomerRiskReport> findCustomers(String userStatus, String accountStatus, String country, int page, int pageSize) {
            org.assertj.core.api.Assertions.assertThat(userStatus).isEqualTo("BLOCKED");
            org.assertj.core.api.Assertions.assertThat(accountStatus).isEqualTo(expectedAccountStatus);
            org.assertj.core.api.Assertions.assertThat(country).isEqualTo("ARG");
            org.assertj.core.api.Assertions.assertThat(page).isEqualTo(1);
            org.assertj.core.api.Assertions.assertThat(pageSize).isEqualTo(50);
            return response;
        }
    }
}
