package com.example.riskconsole.infrastructure.pomelo;

import reactor.core.publisher.Mono;

public interface PomeloDataGateway {
    Mono<PomeloPageResponse<PomeloUserDto>> searchUsers(String userStatus, int page, int pageSize);

    Mono<PomeloPageResponse<PomeloAccountDto>> listAccounts(String country, String userId, String accountStatusCsv);
}
