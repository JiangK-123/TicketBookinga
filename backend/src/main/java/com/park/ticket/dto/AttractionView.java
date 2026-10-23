package com.park.ticket.dto;

import com.park.ticket.domain.Attraction;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "游乐项目")
public record AttractionView(
        @Schema(example = "1") Long id,
        @Schema(example = "极速云霄") String name,
        @Schema(description = "THRILL、FAMILY、WATER、SHOW", example = "THRILL") String category,
        @Schema(example = "刺激项目") String categoryLabel,
        String description,
        @Schema(description = "单价，元", example = "88.00") BigDecimal price,
        @Schema(description = "每个游玩日的可售张数", example = "80") int dailyQuota,
        @Schema(description = "0 表示无身高限制", example = "140") int minHeightCm,
        @Schema(description = "体验时长，分钟", example = "3") int durationMinutes,
        @Schema(description = "开放时间 HH:mm", example = "10:00") String openTime,
        @Schema(description = "关闭时间 HH:mm", example = "20:30") String closeTime) {

    public static AttractionView from(Attraction attraction) {
        return new AttractionView(
                attraction.id(),
                attraction.name(),
                attraction.category().name(),
                attraction.category().label(),
                attraction.description(),
                attraction.price(),
                attraction.dailyQuota(),
                attraction.minHeightCm(),
                attraction.durationMinutes(),
                attraction.openTime(),
                attraction.closeTime());
    }
}
