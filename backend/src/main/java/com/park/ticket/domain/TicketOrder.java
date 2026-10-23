package com.park.ticket.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TicketOrder(
        String orderNo,
        Long attractionId,
        String attractionName,
        LocalDate visitDate,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        String visitorName,
        String visitorPhone,
        OrderStatus status,
        LocalDateTime createdAt) {

    public TicketOrder withStatus(OrderStatus next) {
        return new TicketOrder(
                orderNo,
                attractionId,
                attractionName,
                visitDate,
                quantity,
                unitPrice,
                totalAmount,
                visitorName,
                visitorPhone,
                next,
                createdAt);
    }
}
