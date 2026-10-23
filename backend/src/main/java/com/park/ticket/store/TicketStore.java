package com.park.ticket.store;

import com.park.ticket.domain.Attraction;
import com.park.ticket.domain.AttractionCategory;
import com.park.ticket.domain.TicketOrder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TicketStore {

    private final Map<Long, Attraction> attractions = new java.util.LinkedHashMap<>();
    private final Map<String, TicketOrder> orders = new ConcurrentHashMap<>();
    private final Map<String, Integer> soldBySlot = new ConcurrentHashMap<>();
    private final AtomicInteger dailySeq = new AtomicInteger();
    private LocalDate seqDate = LocalDate.now();

    public TicketStore() {
        seed();
    }

    public List<Attraction> attractions() {
        return List.copyOf(attractions.values());
    }

    public Optional<Attraction> attraction(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(attractions.get(id));
    }

    public int sold(Long attractionId, LocalDate date) {
        return soldBySlot.getOrDefault(slot(attractionId, date), 0);
    }

    public void addSold(Long attractionId, LocalDate date, int quantity) {
        soldBySlot.merge(slot(attractionId, date), quantity, Integer::sum);
    }

    public void releaseSold(Long attractionId, LocalDate date, int quantity) {
        soldBySlot.computeIfPresent(slot(attractionId, date), (key, sold) -> Math.max(0, sold - quantity));
    }

    public void save(TicketOrder order) {
        orders.put(order.orderNo(), order);
    }

    public Optional<TicketOrder> order(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(orders.get(orderNo));
    }

    public List<TicketOrder> ordersByPhone(String phone) {
        return orders.values().stream()
                .filter(order -> order.visitorPhone().equals(phone))
                .sorted(Comparator.comparing(TicketOrder::createdAt).reversed()
                        .thenComparing(TicketOrder::orderNo, Comparator.reverseOrder()))
                .toList();
    }

    public synchronized String nextOrderNo(LocalDate today) {
        if (!today.equals(seqDate)) {
            seqDate = today;
            dailySeq.set(0);
        }
        int sequence = dailySeq.incrementAndGet();
        return "PK" + today.format(DateTimeFormatter.BASIC_ISO_DATE) + String.format("%04d", sequence);
    }

    private static String slot(Long attractionId, LocalDate date) {
        return attractionId + "|" + date;
    }

    private void seed() {
        add(1L, "极速云霄", AttractionCategory.THRILL, "垂直俯冲接螺旋翻滚，单次约 3 分钟。", "88.00", 80, 140, 3, "10:00", "20:30");
        add(2L, "星河摩天轮", AttractionCategory.FAMILY, "封闭座舱缓缓升空，适合傍晚看园内灯光。", "58.00", 240, 0, 15, "10:00", "21:30");
        add(3L, "彩虹漂流", AttractionCategory.WATER, "环形水道有急弯和小落差，衣物可能溅湿。", "66.00", 100, 110, 8, "10:30", "20:00");
        add(4L, "糖果木马", AttractionCategory.FAMILY, "慢速旋转的木马，低龄儿童可与成人同乘。", "36.00", 180, 0, 4, "09:30", "20:30");
        add(5L, "深海潜艇", AttractionCategory.WATER, "暗场轨道模拟下潜，座舱会轻微摇晃。", "72.00", 20, 120, 6, "11:00", "20:00");
        add(6L, "奇幻灯光秀", AttractionCategory.SHOW, "湖心舞台的定点灯光秀，凭票分区入场。", "45.00", 400, 0, 20, "19:30", "20:00");
    }

    private void add(Long id, String name, AttractionCategory category, String description, String price,
                     int dailyQuota, int minHeightCm, int durationMinutes, String openTime, String closeTime) {
        attractions.put(id, new Attraction(
                id,
                name,
                category,
                description,
                new BigDecimal(price),
                dailyQuota,
                minHeightCm,
                durationMinutes,
                openTime,
                closeTime));
    }
}
