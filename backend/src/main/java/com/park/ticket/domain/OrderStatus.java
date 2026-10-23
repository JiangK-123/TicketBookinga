package com.park.ticket.domain;

public enum OrderStatus {
    UNPAID("待支付"),
    PAID("已支付"),
    CANCELLED("已取消");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
