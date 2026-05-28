package com.example.riskconsole.domain;

import java.time.Instant;

public record AuditEntry(String id, Instant createdAt, String action, String details) {}
