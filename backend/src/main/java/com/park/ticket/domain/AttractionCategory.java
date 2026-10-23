package com.park.ticket.domain;

import java.util.Arrays;
import java.util.Optional;

public enum AttractionCategory {
    THRILL("刺激项目"),
    FAMILY("亲子休闲"),
    WATER("水上项目"),
    SHOW("演出巡游");

    private final String label;

    AttractionCategory(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static Optional<AttractionCategory> fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.name().equals(code))
                .findFirst();
    }
}
