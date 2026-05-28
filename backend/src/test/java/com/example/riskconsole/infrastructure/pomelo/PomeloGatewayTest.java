package com.example.riskconsole.infrastructure.pomelo;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class PomeloGatewayTest {
    @Test
    void searchesUsersWithStatusAndPaginationFilters() {
        var capturedUri = new AtomicReference<URI>();
        var client = WebClient.builder()
                .exchangeFunction(request -> {
                    capturedUri.set(request.url());
                    return Mono.just(jsonResponse("""
                            {
                              "data": [
                                {
                                  "id": "usr-1",
                                  "name": "Juan",
                                  "surname": "Perez",
                                  "email": "juan@example.com",
                                  "identification_type": "DNI",
                                  "identification_value": "12345678",
                                  "tax_identification_type": "CUIL",
                                  "tax_identification_value": "20123456789",
                                  "external_id": "client-1",
                                  "status": "BLOCKED"
                                }
                              ],
                              "meta": { "pagination": { "current_page": 1, "total_pages": 1, "page_size": 50 } }
                            }
                            """));
                })
                .build();

        StepVerifier.create(new PomeloGateway(client).searchUsers("BLOCKED", 1, 50))
                .assertNext(response -> {
                    assertThat(response.data()).hasSize(1);
                    assertThat(response.data().getFirst().id()).isEqualTo("usr-1");
                    assertThat(response.meta().pagination().page_size()).isEqualTo(50);
                })
                .verifyComplete();

        assertThat(capturedUri.get().getPath()).isEqualTo("/users/v1/");
        assertThat(capturedUri.get().getRawQuery()).contains("filter%5Bstatus%5D=BLOCKED");
        assertThat(capturedUri.get().getRawQuery()).contains("page%5Bnumber%5D=1");
        assertThat(capturedUri.get().getRawQuery()).contains("page%5Bsize%5D=50");
    }

    @Test
    void listsAccountsWithCountryUserAndStatusFilters() {
        var capturedUri = new AtomicReference<URI>();
        var client = WebClient.builder()
                .exchangeFunction(request -> {
                    capturedUri.set(request.url());
                    return Mono.just(jsonResponse("""
                            {
                              "data": [
                                {
                                  "id": "acc-1",
                                  "country": "ARG",
                                  "balance": "100.00",
                                  "status": "FROZEN",
                                  "currency": "ARS",
                                  "status_update_motive": "OTHER",
                                  "status_update_comment": "Risk review",
                                  "status_updated_by": "CLIENT",
                                  "updated_at": "2024-01-01T00:00:00Z"
                                }
                              ],
                              "meta": { "pagination": { "current_page": 1, "total_pages": 1, "page_size": 100 } }
                            }
                            """));
                })
                .build();

        StepVerifier.create(new PomeloGateway(client).listAccounts("ARG", "usr-1", "ACTIVE,FROZEN"))
                .assertNext(response -> {
                    assertThat(response.data()).hasSize(1);
                    assertThat(response.data().getFirst().id()).isEqualTo("acc-1");
                    assertThat(response.data().getFirst().status_update_comment()).isEqualTo("Risk review");
                })
                .verifyComplete();

        assertThat(capturedUri.get().getPath()).isEqualTo("/core/accounts/v1");
        assertThat(capturedUri.get().getRawQuery()).contains("filter%5Bcountry%5D=ARG");
        assertThat(capturedUri.get().getRawQuery()).contains("filter%5Buser_id%5D=usr-1");
        assertThat(capturedUri.get().getRawQuery()).contains("filter%5Bstatus%5D=ACTIVE,FROZEN");
        assertThat(capturedUri.get().getRawQuery()).contains("page%5Bsize%5D=100");
    }

    private static ClientResponse jsonResponse(String body) {
        return ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(body)
                .build();
    }
}
