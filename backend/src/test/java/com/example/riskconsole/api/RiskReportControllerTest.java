package com.example.riskconsole.api;

import com.example.riskconsole.application.RiskReportService;
import com.example.riskconsole.domain.CustomerRiskReport;
import org.junit.jupiter.api.Test;
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

    private static class FakeRiskReportService extends RiskReportService {
        private final Flux<CustomerRiskReport> response;

        private FakeRiskReportService(Flux<CustomerRiskReport> response) {
            super(null);
            this.response = response;
        }

        @Override
        public Flux<CustomerRiskReport> findCustomers(String userStatus, String accountStatus, String country, int page, int pageSize) {
            org.assertj.core.api.Assertions.assertThat(userStatus).isEqualTo("BLOCKED");
            org.assertj.core.api.Assertions.assertThat(accountStatus).isEqualTo("ACTIVE");
            org.assertj.core.api.Assertions.assertThat(country).isEqualTo("ARG");
            org.assertj.core.api.Assertions.assertThat(page).isEqualTo(1);
            org.assertj.core.api.Assertions.assertThat(pageSize).isEqualTo(50);
            return response;
        }
    }
}
