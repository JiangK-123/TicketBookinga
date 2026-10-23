package com.park.ticket.domain;

import java.math.BigDecimal;

public record Attraction(
        Long id,
        String name,
        AttractionCategory category,
        String description,
        BigDecimal price,
        int dailyQuota,
        int minHeightCm,
        int durationMinutes,
        String openTime,
        String closeTime) {
}
