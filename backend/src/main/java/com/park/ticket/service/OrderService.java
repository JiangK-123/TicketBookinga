package com.park.ticket.service;

import com.park.ticket.api.ApiException;
import com.park.ticket.domain.Attraction;
import com.park.ticket.domain.OrderStatus;
import com.park.ticket.domain.TicketOrder;
import com.park.ticket.dto.CreateOrderRequest;
import com.park.ticket.dto.OrderView;
import com.park.ticket.store.TicketStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {

    private final TicketStore store;
    private final AttractionService attractions;
    private final VisitDatePolicy visitDatePolicy;
    private final Clock clock;
    private final ConcurrentHashMap<String, Object> slotLocks = new ConcurrentHashMap<>();

    public OrderService(TicketStore store, AttractionService attractions, VisitDatePolicy visitDatePolicy, Clock clock) {
        this.store = store;
        this.attractions = attractions;
        this.visitDatePolicy = visitDatePolicy;
        this.clock = clock;
    }

    public OrderView create(CreateOrderRequest request) {
        Attraction attraction = attractions.require(request.attractionId());
        visitDatePolicy.check(request.visitDate());
        synchronized (lock(attraction.id(), request.visitDate())) {
            int remaining = attraction.dailyQuota() - store.sold(attraction.id(), request.visitDate());
            if (request.quantity() > remaining) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "SOLD_OUT",
                        "余票不足，当日剩余 " + Math.max(remaining, 0) + " 张");
            }
            LocalDate today = visitDatePolicy.today();
            BigDecimal total = attraction.price().multiply(BigDecimal.valueOf(request.quantity()));
            TicketOrder order = new TicketOrder(
                    store.nextOrderNo(today),
                    attraction.id(),
                    attraction.name(),
                    request.visitDate(),
                    request.quantity(),
                    attraction.price(),
                    total,
                    request.visitorName(),
                    request.visitorPhone(),
                    OrderStatus.UNPAID,
                    LocalDateTime.now(clock));
            store.addSold(attraction.id(), request.visitDate(), request.quantity());
            store.save(order);
            return OrderView.from(order);
        }
    }

    public List<OrderView> listByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", "请提供下单手机号");
        }
        String normalized = phone.trim();
        if (!normalized.matches("^1\\d{10}$")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "手机号须为 1 开头的 11 位数字");
        }
        return store.ordersByPhone(normalized).stream().map(OrderView::from).toList();
    }

    public OrderView get(String orderNo) {
        return OrderView.from(require(orderNo));
    }

    public OrderView pay(String orderNo) {
        TicketOrder existing = require(orderNo);
        synchronized (lock(existing.attractionId(), existing.visitDate())) {
            TicketOrder current = require(orderNo);
            if (current.status() != OrderStatus.UNPAID) {
                throw new ApiException(HttpStatus.CONFLICT, "ILLEGAL_STATE", "仅待支付订单可以支付");
            }
            TicketOrder paid = current.withStatus(OrderStatus.PAID);
            store.save(paid);
            return OrderView.from(paid);
        }
    }

    public OrderView cancel(String orderNo) {
        TicketOrder existing = require(orderNo);
        synchronized (lock(existing.attractionId(), existing.visitDate())) {
            TicketOrder current = require(orderNo);
            if (current.status() != OrderStatus.UNPAID) {
                throw new ApiException(HttpStatus.CONFLICT, "ILLEGAL_STATE", "仅待支付订单可以取消");
            }
            TicketOrder cancelled = current.withStatus(OrderStatus.CANCELLED);
            store.save(cancelled);
            store.releaseSold(current.attractionId(), current.visitDate(), current.quantity());
            return OrderView.from(cancelled);
        }
    }

    private TicketOrder require(String orderNo) {
        return store.order(orderNo)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "订单不存在"));
    }

    private Object lock(Long attractionId, LocalDate date) {
        return slotLocks.computeIfAbsent(attractionId + "|" + date, key -> new Object());
    }
}
