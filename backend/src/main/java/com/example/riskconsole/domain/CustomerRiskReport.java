package com.example.riskconsole.domain;

import java.util.List;

public record CustomerRiskReport(
        String userId,
        String externalId,
        String fullName,
        String email,
        String identification,
        String taxIdentification,
        String userStatus,
        List<AccountRiskDetail> accounts
) {}
