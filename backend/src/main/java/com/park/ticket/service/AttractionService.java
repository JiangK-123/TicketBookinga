package com.park.ticket.service;

import com.park.ticket.api.ApiException;
import com.park.ticket.domain.Attraction;
import com.park.ticket.domain.AttractionCategory;
import com.park.ticket.dto.AttractionView;
import com.park.ticket.dto.AvailabilityView;
import com.park.ticket.dto.CategoryView;
import com.park.ticket.store.TicketStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class AttractionService {

    private final TicketStore store;
    private final VisitDatePolicy visitDatePolicy;

    public AttractionService(TicketStore store, VisitDatePolicy visitDatePolicy) {
        this.store = store;
        this.visitDatePolicy = visitDatePolicy;
    }

    public List<CategoryView> categories() {
        return Arrays.stream(AttractionCategory.values())
                .map(item -> new CategoryView(item.name(), item.label()))
                .toList();
    }

    public List<AttractionView> list(String category, String keyword) {
        AttractionCategory categoryFilter = parseCategory(category);
        String needle = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return store.attractions().stream()
                .filter(item -> categoryFilter == null || item.category() == categoryFilter)
                .filter(item -> needle.isEmpty() || item.name().toLowerCase(Locale.ROOT).contains(needle))
                .map(AttractionView::from)
                .toList();
    }

    public AttractionView get(Long id) {
        return AttractionView.from(require(id));
    }

    public AvailabilityView availability(Long id, LocalDate date) {
        Attraction attraction = require(id);
        visitDatePolicy.check(date);
        int sold = store.sold(id, date);
        int remaining = Math.max(0, attraction.dailyQuota() - sold);
        return new AvailabilityView(id, date, attraction.dailyQuota(), sold, remaining);
    }

    Attraction require(Long id) {
        return store.attraction(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "游乐项目不存在"));
    }

    private AttractionCategory parseCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        return AttractionCategory.fromCode(category.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CATEGORY", "未知的项目分类"));
    }
}
