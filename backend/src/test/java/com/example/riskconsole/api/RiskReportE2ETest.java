package com.example.riskconsole.api;

import com.example.riskconsole.application.RiskReportService;
import com.example.riskconsole.domain.CustomerRiskReport;
import com.example.riskconsole.infrastructure.dev.DevPomeloGateway;
import com.example.riskconsole.infrastructure.pomelo.PomeloAccountDto;
import com.example.riskconsole.infrastructure.pomelo.PomeloUserDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RiskReportE2ETest {

    private static final String INPUT_DIR = "fixtures/input/";
    private static final String OUTPUT_DIR = "fixtures/output/";

    private final ObjectMapper mapper = new ObjectMapper();

    private DevPomeloGateway gateway;
    private RiskReportService service;
    private RiskReportController controller;

    @BeforeEach
    void setUp() throws IOException {
        var users = mapper.readValue(
                resource(INPUT_DIR + "pomelo-users.json"),
                new TypeReference<List<PomeloUserDto>>() {}
        );
        var accounts = mapper.readValue(
                resource(INPUT_DIR + "pomelo-accounts.json"),
                new TypeReference<Map<String, List<PomeloAccountDto>>>() {}
        );
        gateway = new DevPomeloGateway(users, accounts);
        service = new RiskReportService(gateway);
        controller = new RiskReportController(service);
    }

    @Test
    void returnsBlockedUsersWithAccountDetailsUsingDefaultFilters() {
        var client = WebTestClient.bindToController(controller).build();

        var expected = readExpected("risk-report-blocked-users.json");

        client.get()
                .uri("/api/risk/customers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerRiskReport.class)
                .hasSize(expected.size())
                .consumeWith(result -> {
                    var actual = result.getResponseBody();
                    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
                });
    }

    @Test
    void filtersByAccountStatusReturningOnlyMatchingAccounts() {
        var client = WebTestClient.bindToController(controller).build();

        client.get()
                .uri("/api/risk/customers?userStatus=BLOCKED&accountStatus=FROZEN")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerRiskReport.class)
                .hasSize(2)
                .consumeWith(result -> {
                    var lucia = result.getResponseBody().getFirst();
                    assertThat(lucia.userId()).isEqualTo("usr-dev-blocked-1");
                    assertThat(lucia.accounts()).hasSize(1);
                    assertThat(lucia.accounts().getFirst().accountStatus()).isEqualTo("FROZEN");

                    var ana = result.getResponseBody().getLast();
                    assertThat(ana.userId()).isEqualTo("usr-dev-blocked-2");
                    assertThat(ana.accounts()).isEmpty();
                });
    }

    @Test
    void returnsActiveUsersWhenFilteringByUserStatus() {
        var client = WebTestClient.bindToController(controller).build();

        client.get()
                .uri("/api/risk/customers?userStatus=ACTIVE")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerRiskReport.class)
                .hasSize(1)
                .consumeWith(result -> {
                    var report = result.getResponseBody().getFirst();
                    assertThat(report.userId()).isEqualTo("usr-dev-active-1");
                    assertThat(report.fullName()).isEqualTo("Martin Silva");
                    assertThat(report.userStatus()).isEqualTo("ACTIVE");
                    assertThat(report.accounts()).hasSize(1);
                    assertThat(report.accounts().getFirst().accountStatus()).isEqualTo("ACTIVE");
                });
    }

    @Test
    void returnsEmptyWhenNoUsersMatchUserStatusFilter() {
        var client = WebTestClient.bindToController(controller).build();

        client.get()
                .uri("/api/risk/customers?userStatus=PENDING")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerRiskReport.class)
                .hasSize(0);
    }

    @Test
    void returnsEmptyAccountsListWhenNoAccountsMatchStatusFilter() {
        var client = WebTestClient.bindToController(controller).build();

        client.get()
                .uri("/api/risk/customers?userStatus=BLOCKED&accountStatus=ACTIVE")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerRiskReport.class)
                .hasSize(2)
                .consumeWith(result -> {
                    var reports = result.getResponseBody();
                    assertThat(reports).allMatch(r -> r.accounts().isEmpty());
                });
    }

    @Test
    void buildsReportForUserWithoutAccounts() {
        var users = List.of(
                new PomeloUserDto("usr-no-acc", "No", "Accounts", "no@test.com",
                        "DNI", "1", "CUIL", "2", "ext-no-acc", "BLOCKED")
        );
        var noAccGateway = new DevPomeloGateway(users, Map.of());
        var noAccService = new RiskReportService(noAccGateway);
        var noAccController = new RiskReportController(noAccService);
        var client = WebTestClient.bindToController(noAccController).build();

        StepVerifier.create(noAccController.search("key", "BLOCKED", "ACTIVE", "ARG", 1, 50))
                .assertNext(report -> {
                    assertThat(report.userId()).isEqualTo("usr-no-acc");
                    assertThat(report.accounts()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void propagatesRequestHeadersAndQueryParamsToService() {
        var client = WebTestClient.bindToController(controller).build();

        client.get()
                .uri("/api/risk/customers?userStatus=ACTIVE&accountStatus=ACTIVE&country=ARG&page=2&pageSize=10")
                .header("X-Internal-Api-Key", "test-key")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerRiskReport.class)
                .hasSize(1);
    }

    private List<CustomerRiskReport> readExpected(String filename) {
        try {
            var reports = mapper.readValue(
                    resource(OUTPUT_DIR + filename),
                    CustomerRiskReport[].class
            );
            return List.of(reports);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read expected fixture: " + filename, e);
        }
    }

    private java.io.InputStream resource(String path) {
        var resource = getClass().getClassLoader().getResourceAsStream(path);
        if (resource == null) {
            throw new RuntimeException("Resource not found: " + path);
        }
        return resource;
    }
}
