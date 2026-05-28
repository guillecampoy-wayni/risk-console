package com.example.riskconsole.domain;

public record AccountRiskDetail(
        String accountId,
        String country,
        String currency,
        String balance,
        String accountStatus,
        String statusUpdateMotive,
        String statusUpdateComment,
        String statusUpdatedBy,
        String updatedAt
) {}
