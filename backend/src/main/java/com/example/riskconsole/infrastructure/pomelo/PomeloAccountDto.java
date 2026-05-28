package com.example.riskconsole.infrastructure.pomelo;

public record PomeloAccountDto(
        String id,
        String country,
        String balance,
        String status,
        String currency,
        String status_update_motive,
        String status_update_comment,
        String status_updated_by,
        String updated_at
) {}
