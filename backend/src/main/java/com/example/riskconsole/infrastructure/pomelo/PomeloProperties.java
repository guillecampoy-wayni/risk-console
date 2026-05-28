package com.example.riskconsole.infrastructure.pomelo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pomelo")
public record PomeloProperties(String baseUrl, String apiKey) {}
