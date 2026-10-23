package com.park.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "某一天的余票")
public record AvailabilityView(
        Long attractionId,
        @Schema(example = "2026-09-25") LocalDate date,
        int dailyQuota,
        int sold,
        int remaining) {
}
