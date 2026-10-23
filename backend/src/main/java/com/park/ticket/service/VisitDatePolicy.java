package com.park.ticket.service;

import com.park.ticket.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class VisitDatePolicy {

    static final int MAX_DAYS_AHEAD = 30;

    private final Clock clock;

    public VisitDatePolicy(Clock clock) {
        this.clock = clock;
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public void check(LocalDate date) {
        LocalDate today = today();
        if (date.isBefore(today)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_DATE", "游玩日期不能早于今天");
        }
        if (date.isAfter(today.plusDays(MAX_DAYS_AHEAD))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_DATE", "游玩日期须在今天至其后 30 天之间");
        }
    }
}
