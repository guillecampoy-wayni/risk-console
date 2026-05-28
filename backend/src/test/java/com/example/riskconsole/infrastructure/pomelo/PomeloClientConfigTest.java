package com.example.riskconsole.infrastructure.pomelo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PomeloClientConfigTest {
    @Test
    void buildsPomeloWebClientFromConfiguredProperties() {
        var client = new PomeloClientConfig()
                .pomeloWebClient(new PomeloProperties("https://api.pomelo.la", "secret-key"));

        assertThat(client).isNotNull();
    }
}
