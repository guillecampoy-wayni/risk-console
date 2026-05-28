package com.example.riskconsole.infrastructure.dev;

import com.example.riskconsole.infrastructure.pomelo.PomeloAccountDto;
import com.example.riskconsole.infrastructure.pomelo.PomeloGateway;
import com.example.riskconsole.infrastructure.pomelo.PomeloUserDto;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DevPomeloGatewayTest {
    private static final List<PomeloUserDto> USERS = List.of(
            new PomeloUserDto("u1", "Alice", "A", "alice@test.com", "DNI", "1", "CUIL", "2", "e1", "BLOCKED"),
            new PomeloUserDto("u2", "Bob", "B", "bob@test.com", "DNI", "3", "CUIL", "4", "e2", "ACTIVE")
    );

    private static final Map<String, List<PomeloAccountDto>> ACCOUNTS = Map.of(
            "u1", List.of(
                    new PomeloAccountDto("a1", "ARG", "100.00", "FROZEN", "ARS", "OTHER", "review", "CLIENT", "2024-01-01T00:00:00Z"),
                    new PomeloAccountDto("a2", "ARG", "200.00", "DISABLED", "USD", "FRAUD", "investigation", "POMELO", "2024-01-02T00:00:00Z")
            ),
            "u2", List.of(
                    new PomeloAccountDto("a3", "ARG", "300.00", "ACTIVE", "ARS", null, null, null, "2024-01-03T00:00:00Z")
            )
    );

    @Test
    void exposesRepresentativeUsersFilteredByRequestedStatus() {
        var gateway = new DevPomeloGateway(USERS, ACCOUNTS);

        StepVerifier.create(gateway.searchUsers("BLOCKED", 1, 50))
                .assertNext(response -> {
                    assertThat(response.data()).isNotEmpty();
                    assertThat(response.data()).allMatch(user -> "BLOCKED".equals(user.status()));
                    assertThat(response.meta().pagination().page_size()).isEqualTo(50);
                })
                .verifyComplete();
    }

    @Test
    void exposesRepresentativeAccountsFilteredByCountryUserAndStatus() {
        var gateway = new DevPomeloGateway(USERS, ACCOUNTS);

        StepVerifier.create(gateway.listAccounts("ARG", "u1", "FROZEN,DISABLED"))
                .assertNext(response -> {
                    assertThat(response.data()).isNotEmpty();
                    assertThat(response.data()).allMatch(account -> "ARG".equals(account.country()));
                    assertThat(response.data()).allMatch(account -> account.status().equals("FROZEN") || account.status().equals("DISABLED"));
                    assertThat(response.meta().pagination().page_size()).isEqualTo(100);
                })
                .verifyComplete();
    }

    @Test
    void returnsEmptyWhenNoUsersMatchStatusFilter() {
        var gateway = new DevPomeloGateway(USERS, ACCOUNTS);

        StepVerifier.create(gateway.searchUsers("INACTIVE", 1, 50))
                .assertNext(response -> {
                    assertThat(response.data()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void returnsEmptyWhenNoAccountsMatchStatusFilter() {
        var gateway = new DevPomeloGateway(USERS, ACCOUNTS);

        StepVerifier.create(gateway.listAccounts("ARG", "u1", "ACTIVE"))
                .assertNext(response -> {
                    assertThat(response.data()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void returnsEmptyWhenUserHasNoAccounts() {
        var users = List.of(
                new PomeloUserDto("u3", "Carol", "C", "carol@test.com", "DNI", "5", "CUIL", "6", "e3", "BLOCKED")
        );
        var gateway = new DevPomeloGateway(users, ACCOUNTS);

        StepVerifier.create(gateway.listAccounts("ARG", "u3", "ACTIVE"))
                .assertNext(response -> {
                    assertThat(response.data()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void separatesDevelopmentAndProductionGatewaysBySpringProfile() {
        assertThat(DevPomeloGateway.class.getAnnotation(Profile.class).value()).contains("dev");
        assertThat(PomeloGateway.class.getAnnotation(Profile.class).value()).contains("prod");
    }
}
