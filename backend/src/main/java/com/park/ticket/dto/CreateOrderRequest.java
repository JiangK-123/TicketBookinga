package com.park.ticket.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "创建订单。未知字段会被忽略。姓名和手机号会先去掉首尾空格。")
public record CreateOrderRequest(
        @NotNull(message = "请选择游乐项目")
        @Schema(example = "1")
        Long attractionId,
        @NotNull(message = "请选择游玩日期")
        @Schema(description = "yyyy-MM-dd，今天起 30 天内", example = "2026-09-25")
        LocalDate visitDate,
        @NotNull(message = "请填写购票数量")
        @Min(value = 1, message = "购票数量至少为 1")
        @Max(value = 10, message = "单笔订单最多 10 张")
        @Schema(minimum = "1", maximum = "10", example = "2")
        Integer quantity,
        @NotBlank(message = "请填写游客姓名")
        @Size(max = 20, message = "姓名不超过 20 个字符")
        @Pattern(regexp = "^[^0-9０-９]+$", message = "姓名不能包含数字")
        @Schema(description = "不能包含数字，包括全角数字", maxLength = 20, example = "张三")
        String visitorName,
        @NotBlank(message = "请填写手机号")
        @Pattern(regexp = "^1\\d{10}$", message = "手机号须为 1 开头的 11 位数字")
        @Schema(example = "13800138000")
        String visitorPhone) {

    public CreateOrderRequest {
        if (visitorName != null) {
            visitorName = visitorName.trim();
        }
        if (visitorPhone != null) {
            visitorPhone = visitorPhone.trim();
        }
    }
}
