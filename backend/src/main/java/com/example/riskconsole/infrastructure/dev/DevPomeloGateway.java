package com.example.riskconsole.infrastructure.dev;

import com.example.riskconsole.infrastructure.pomelo.PomeloAccountDto;
import com.example.riskconsole.infrastructure.pomelo.PomeloDataGateway;
import com.example.riskconsole.infrastructure.pomelo.PomeloPageResponse;
import com.example.riskconsole.infrastructure.pomelo.PomeloUserDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Profile("dev")
public class DevPomeloGateway implements PomeloDataGateway {
    private final List<PomeloUserDto> users;
    private final Map<String, List<PomeloAccountDto>> accountsByUser;

    @Autowired
    public DevPomeloGateway(ObjectMapper objectMapper) {
        try {
            var usersResource = new ClassPathResource("devdata/pomelo-users.json");
            this.users = objectMapper.readValue(
                    usersResource.getInputStream(),
                    new TypeReference<List<PomeloUserDto>>() {}
            );
            var accountsResource = new ClassPathResource("devdata/pomelo-accounts.json");
            this.accountsByUser = objectMapper.readValue(
                    accountsResource.getInputStream(),
                    new TypeReference<Map<String, List<PomeloAccountDto>>>() {}
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load dev data from JSON files", e);
        }
    }

    public DevPomeloGateway(List<PomeloUserDto> users, Map<String, List<PomeloAccountDto>> accountsByUser) {
        this.users = users;
        this.accountsByUser = accountsByUser;
    }

    @Override
    public Mono<PomeloPageResponse<PomeloUserDto>> searchUsers(String userStatus, int page, int pageSize) {
        var requestedStatuses = csvValues(userStatus);
        var filtered = users.stream()
                .filter(user -> requestedStatuses.contains(user.status()))
                .toList();

        return Mono.just(page(filtered, page, pageSize));
    }

    @Override
    public Mono<PomeloPageResponse<PomeloAccountDto>> listAccounts(String country, String userId, String accountStatusCsv) {
        var requestedStatuses = csvValues(accountStatusCsv);
        var userAccounts = accountsByUser.getOrDefault(userId, List.of());
        var filtered = userAccounts.stream()
                .filter(account -> account.country().equals(country))
                .filter(account -> requestedStatuses.contains(account.status()))
                .toList();

        return Mono.just(page(filtered, 1, 100));
    }

    private static Set<String> csvValues(String csv) {
        return Set.of(csv.split(","));
    }

    private static <T> PomeloPageResponse<T> page(List<T> data, int page, int pageSize) {
        return new PomeloPageResponse<>(
                data,
                new PomeloPageResponse.Meta(new PomeloPageResponse.Pagination(page, 1, pageSize))
        );
    }
}
