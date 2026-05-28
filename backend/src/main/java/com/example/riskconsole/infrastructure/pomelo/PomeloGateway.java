package com.example.riskconsole.infrastructure.pomelo;

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Profile("prod")
public class PomeloGateway implements PomeloDataGateway {
    private final WebClient client;

    public PomeloGateway(WebClient pomeloWebClient) {
        this.client = pomeloWebClient;
    }

    @Override
    public Mono<PomeloPageResponse<PomeloUserDto>> searchUsers(String userStatus, int page, int pageSize) {
        return client.get()
                .uri(uri -> uri.path("/users/v1/")
                        .queryParam("filter[status]", userStatus)
                        .queryParam("page[number]", page)
                        .queryParam("page[size]", pageSize)
                        .build())
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<>() {});
    }

    @Override
    public Mono<PomeloPageResponse<PomeloAccountDto>> listAccounts(String country, String userId, String accountStatusCsv) {
        return client.get()
                .uri(uri -> uri.path("/core/accounts/v1")
                        .queryParam("filter[country]", country)
                        .queryParam("filter[user_id]", userId)
                        .queryParam("filter[status]", accountStatusCsv)
                        .queryParam("page[number]", 1)
                        .queryParam("page[size]", 100)
                        .build())
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<>() {});
    }
}
