package com.park.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "项目分类")
public record CategoryView(
        @Schema(example = "THRILL") String code,
        @Schema(example = "刺激项目") String label) {
}
