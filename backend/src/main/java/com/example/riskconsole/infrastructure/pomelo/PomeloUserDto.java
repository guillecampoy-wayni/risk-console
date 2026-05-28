package com.example.riskconsole.infrastructure.pomelo;

public record PomeloUserDto(
        String id,
        String name,
        String surname,
        String email,
        String identification_type,
        String identification_value,
        String tax_identification_type,
        String tax_identification_value,
        String external_id,
        String status
) {}
