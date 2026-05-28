package com.example.riskconsole.domain;

import java.time.Instant;

public record Snapshot(String id, Instant createdAt) {}
