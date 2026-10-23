package com.park.ticket.dto;

import com.park.ticket.domain.TicketOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "订单")
public record OrderView(
        @Schema(description = "订单号，PK + 下单日期 yyyyMMdd + 4 位序号", example = "PK202609240001")
        String orderNo,
        Long attractionId,
        String attractionName,
        LocalDate visitDate,
        int quantity,
        @Schema(description = "单价，元") BigDecimal unitPrice,
        @Schema(description = "总价，元，等于单价乘以数量") BigDecimal totalAmount,
        String visitorName,
        String visitorPhone,
        @Schema(description = "UNPAID、PAID、CANCELLED") String status,
        String statusLabel,
        @Schema(description = "本地时间，无时区后缀") LocalDateTime createdAt) {

    public static OrderView from(TicketOrder order) {
        return new OrderView(
                order.orderNo(),
                order.attractionId(),
                order.attractionName(),
                order.visitDate(),
                order.quantity(),
                order.unitPrice(),
                order.totalAmount(),
                order.visitorName(),
                order.visitorPhone(),
                order.status().name(),
                order.status().label(),
                order.createdAt());
    }
}
