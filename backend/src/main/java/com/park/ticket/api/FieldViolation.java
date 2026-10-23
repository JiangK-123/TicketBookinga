package com.park.ticket.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "字段级错误")
public record FieldViolation(
        @Schema(example = "visitorPhone") String field,
        @Schema(example = "手机号须为 1 开头的 11 位数字") String message) {
}
