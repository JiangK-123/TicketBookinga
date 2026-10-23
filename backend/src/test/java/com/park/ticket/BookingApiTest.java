package com.park.ticket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingApiTest {

    private static final String ORIGIN = "http://localhost:5173";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listsAttractionsWithReviewedFields() throws Exception {
        mockMvc.perform(get("/api/attractions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("极速云霄"))
                .andExpect(jsonPath("$[0].category").value("THRILL"))
                .andExpect(jsonPath("$[0].categoryLabel").value("刺激项目"))
                .andExpect(jsonPath("$[0].price").value(88.00))
                .andExpect(jsonPath("$[0].dailyQuota").value(80))
                .andExpect(jsonPath("$[0].minHeightCm").value(140))
                .andExpect(jsonPath("$[0].durationMinutes").value(3))
                .andExpect(jsonPath("$[0].openTime").value("10:00"))
                .andExpect(jsonPath("$[0].closeTime").value("20:30"))
                .andExpect(jsonPath("$[4].name").value("深海潜艇"))
                .andExpect(jsonPath("$[4].dailyQuota").value(20));
    }

    @Test
    void filtersByCategoryAndKeyword() throws Exception {
        mockMvc.perform(get("/api/attractions").param("category", "WATER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("彩虹漂流"))
                .andExpect(jsonPath("$[1].name").value("深海潜艇"));

        mockMvc.perform(get("/api/attractions").param("keyword", "摩天轮"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("FAMILY"));
    }

    @Test
    void rejectsUnknownCategory() throws Exception {
        mockMvc.perform(get("/api/attractions").param("category", "NIGHT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CATEGORY"))
                .andExpect(jsonPath("$.message").value("未知的项目分类"))
                .andExpect(jsonPath("$.details", hasSize(0)))
                .andExpect(jsonPath("$.path").value("/api/attractions"));
    }

    @Test
    void getsAttractionOrNotFound() throws Exception {
        mockMvc.perform(get("/api/attractions/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("星河摩天轮"))
                .andExpect(jsonPath("$.minHeightCm").value(0));

        mockMvc.perform(get("/api/attractions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("游乐项目不存在"));
    }

    @Test
    void listsCategories() throws Exception {
        mockMvc.perform(get("/api/attractions/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].code").value("THRILL"))
                .andExpect(jsonPath("$[0].label").value("刺激项目"))
                .andExpect(jsonPath("$[3].code").value("SHOW"));
    }

    @Test
    void availabilityMatchesQuotaBeforeAnySale() throws Exception {
        mockMvc.perform(get("/api/attractions/6/availability").param("date", day(20)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attractionId").value(6))
                .andExpect(jsonPath("$.date").value(day(20)))
                .andExpect(jsonPath("$.dailyQuota").value(400))
                .andExpect(jsonPath("$.sold").value(0))
                .andExpect(jsonPath("$.remaining").value(400));
    }

    @Test
    void rejectsDatesOutsideBookingWindow() throws Exception {
        mockMvc.perform(get("/api/attractions/6/availability").param("date", day(-1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DATE"))
                .andExpect(jsonPath("$.message").value("游玩日期不能早于今天"));

        mockMvc.perform(get("/api/attractions/6/availability").param("date", day(30)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remaining").value(400));

        mockMvc.perform(get("/api/attractions/6/availability").param("date", day(31)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DATE"))
                .andExpect(jsonPath("$.message").value("游玩日期须在今天至其后 30 天之间"));
    }

    @Test
    void rejectsMissingOrMalformedDateAndId() throws Exception {
        mockMvc.perform(get("/api/attractions/1/availability"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"))
                .andExpect(jsonPath("$.message").value("缺少参数 date，格式应为 yyyy-MM-dd"))
                .andExpect(jsonPath("$.details[0].field").value("date"));

        mockMvc.perform(get("/api/attractions/1/availability").param("date", "2026/09/24"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TYPE_MISMATCH"))
                .andExpect(jsonPath("$.message").value("参数 date 格式应为 yyyy-MM-dd"));

        mockMvc.perform(get("/api/attractions/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TYPE_MISMATCH"))
                .andExpect(jsonPath("$.message").value("参数 id 必须是数字"));
    }

    @Test
    void createsOrderAndReservesQuota() throws Exception {
        String date = day(2);
        MvcResult result = postOrder(1, date, 2, "张三", "13800138001")
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString("/api/orders/PK")))
                .andExpect(jsonPath("$.attractionId").value(1))
                .andExpect(jsonPath("$.attractionName").value("极速云霄"))
                .andExpect(jsonPath("$.visitDate").value(date))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.unitPrice").value(88.00))
                .andExpect(jsonPath("$.totalAmount").value(176.00))
                .andExpect(jsonPath("$.visitorName").value("张三"))
                .andExpect(jsonPath("$.visitorPhone").value("13800138001"))
                .andExpect(jsonPath("$.status").value("UNPAID"))
                .andExpect(jsonPath("$.statusLabel").value("待支付"))
                .andExpect(jsonPath("$.orderNo").value(containsString(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE))))
                .andReturn();

        String orderNo = json(result).get("orderNo").asText();
        mockMvc.perform(get("/api/orders/{orderNo}", orderNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(176.00));

        mockMvc.perform(get("/api/attractions/1/availability").param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sold").value(2))
                .andExpect(jsonPath("$.remaining").value(78));
    }

    @Test
    void trimsNameAndPhoneAndIgnoresUnknownFields() throws Exception {
        String body = """
                {
                  "attractionId": 2,
                  "visitDate": "%s",
                  "quantity": 1,
                  "visitorName": "  李四  ",
                  "visitorPhone": " 13800138002 ",
                  "note": "忽略我"
                }
                """.formatted(day(3));
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visitorName").value("李四"))
                .andExpect(jsonPath("$.visitorPhone").value("13800138002"))
                .andExpect(jsonPath("$.unitPrice").value(58.00));
    }

    @Test
    void rejectsInvalidOrderFields() throws Exception {
        String body = """
                {
                  "attractionId": null,
                  "visitDate": null,
                  "quantity": 0,
                  "visitorName": "   ",
                  "visitorPhone": "12345"
                }
                """;
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("请求参数不合法"))
                .andExpect(jsonPath("$.details[?(@.field == 'attractionId')].message", hasItem("请选择游乐项目")))
                .andExpect(jsonPath("$.details[?(@.field == 'visitDate')].message", hasItem("请选择游玩日期")))
                .andExpect(jsonPath("$.details[?(@.field == 'quantity')].message", hasItem("购票数量至少为 1")))
                .andExpect(jsonPath("$.details[?(@.field == 'visitorName')].message", hasItem("请填写游客姓名")))
                .andExpect(jsonPath("$.details[?(@.field == 'visitorPhone')].message", hasItem("手机号须为 1 开头的 11 位数字")));

        String tooMany = """
                {
                  "attractionId": 1,
                  "visitDate": "%s",
                  "quantity": 11,
                  "visitorName": "一二三四五六七八九十一二三四五六七八九十一",
                  "visitorPhone": "13800138000"
                }
                """.formatted(day(1));
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(tooMany))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[?(@.field == 'quantity')].message", hasItem("单笔订单最多 10 张")))
                .andExpect(jsonPath("$.details[?(@.field == 'visitorName')].message", hasItem("姓名不超过 20 个字符")));

        String withDigit = """
                {
                  "attractionId": 1,
                  "visitDate": "%s",
                  "quantity": 1,
                  "visitorName": "张三1",
                  "visitorPhone": "13800138011"
                }
                """.formatted(day(12));
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(withDigit))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[?(@.field == 'visitorName')].message", hasItem("姓名不能包含数字")));

        String fullWidthDigit = """
                {
                  "attractionId": 1,
                  "visitDate": "%s",
                  "quantity": 1,
                  "visitorName": "  李四５ ",
                  "visitorPhone": "13800138011"
                }
                """.formatted(day(12));
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(fullWidthDigit))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[?(@.field == 'visitorName')].message", hasItem("姓名不能包含数字")));
    }

    @Test
    void rejectsMalformedJsonAndUnsupportedMedia() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON"));

        mockMvc.perform(post("/api/orders").contentType(MediaType.TEXT_PLAIN).content("hello"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA"));
    }

    @Test
    void rejectsUnknownAttractionAndPastVisitDate() throws Exception {
        postOrder(99, day(1), 1, "王五", "13800138003")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("游乐项目不存在"));

        postOrder(1, day(-1), 1, "王五", "13800138003")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DATE"))
                .andExpect(jsonPath("$.message").value("游玩日期不能早于今天"));
    }

    @Test
    void rejectsSoldOut() throws Exception {
        String date = day(4);
        postOrder(5, date, 10, "赵六", "13800138004").andExpect(status().isCreated());
        postOrder(5, date, 10, "赵六", "13800138004").andExpect(status().isCreated());
        postOrder(5, date, 1, "赵六", "13800138004")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SOLD_OUT"))
                .andExpect(jsonPath("$.message").value("余票不足，当日剩余 0 张"));

        mockMvc.perform(get("/api/attractions/5/availability").param("date", date))
                .andExpect(jsonPath("$.sold").value(20))
                .andExpect(jsonPath("$.remaining").value(0));
    }

    @Test
    void cancelRestoresQuotaAndRejectsRepeat() throws Exception {
        String date = day(5);
        MvcResult created = postOrder(4, date, 2, "钱七", "13800138005")
                .andExpect(status().isCreated())
                .andReturn();
        String orderNo = json(created).get("orderNo").asText();

        mockMvc.perform(get("/api/attractions/4/availability").param("date", date))
                .andExpect(jsonPath("$.remaining").value(178));

        mockMvc.perform(post("/api/orders/{orderNo}/cancel", orderNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.statusLabel").value("已取消"));

        mockMvc.perform(get("/api/attractions/4/availability").param("date", date))
                .andExpect(jsonPath("$.sold").value(0))
                .andExpect(jsonPath("$.remaining").value(180));

        mockMvc.perform(post("/api/orders/{orderNo}/cancel", orderNo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ILLEGAL_STATE"))
                .andExpect(jsonPath("$.message").value("仅待支付订单可以取消"));

        mockMvc.perform(post("/api/orders/{orderNo}/pay", orderNo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("仅待支付订单可以支付"));
    }

    @Test
    void payThenRejectsSecondPayAndCancel() throws Exception {
        MvcResult created = postOrder(2, day(6), 1, "孙八", "13800138006")
                .andExpect(status().isCreated())
                .andReturn();
        String orderNo = json(created).get("orderNo").asText();

        mockMvc.perform(post("/api/orders/{orderNo}/pay", orderNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.statusLabel").value("已支付"));

        mockMvc.perform(post("/api/orders/{orderNo}/pay", orderNo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ILLEGAL_STATE"))
                .andExpect(jsonPath("$.message").value("仅待支付订单可以支付"));

        mockMvc.perform(post("/api/orders/{orderNo}/cancel", orderNo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("仅待支付订单可以取消"));

        mockMvc.perform(get("/api/orders/{orderNo}", orderNo))
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void queriesOrdersByPhone() throws Exception {
        postOrder(1, day(7), 1, "周九", "13800138007").andExpect(status().isCreated());
        postOrder(3, day(8), 1, "周九", "13800138007").andExpect(status().isCreated());
        postOrder(2, day(9), 1, "吴十", "13800138008").andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders").param("phone", "13800138007"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].visitorPhone").value("13800138007"))
                .andExpect(jsonPath("$[1].visitorPhone").value("13800138007"));

        mockMvc.perform(get("/api/orders").param("phone", "13900000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"))
                .andExpect(jsonPath("$.message").value("请提供下单手机号"));

        mockMvc.perform(get("/api/orders").param("phone", " 13800138007 "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/orders").param("phone", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("手机号须为 1 开头的 11 位数字"));
    }

    @Test
    void missingOrderOnGetPayAndCancel() throws Exception {
        mockMvc.perform(get("/api/orders/PK404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("订单不存在"));
        mockMvc.perform(post("/api/orders/PK404/pay"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("订单不存在"));
        mockMvc.perform(post("/api/orders/PK404/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("订单不存在"));
    }

    @Test
    void rejectsUnknownApiPathAndMethod() throws Exception {
        mockMvc.perform(get("/api/no-such"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("接口不存在"));

        mockMvc.perform(post("/api/attractions"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void corsAllowsFrontendOriginAndRejectsOthers() throws Exception {
        mockMvc.perform(get("/api/attractions").header(HttpHeaders.ORIGIN, ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN));

        mockMvc.perform(options("/api/orders")
                        .header(HttpHeaders.ORIGIN, ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("POST")));

        mockMvc.perform(get("/api/attractions").header(HttpHeaders.ORIGIN, "http://evil.example"))
                .andExpect(status().isForbidden());
    }

    private ResultActions postOrder(long attractionId, String date, int quantity, String name, String phone) throws Exception {
        String body = """
                {
                  "attractionId": %d,
                  "visitDate": "%s",
                  "quantity": %d,
                  "visitorName": "%s",
                  "visitorPhone": "%s"
                }
                """.formatted(attractionId, date, quantity, name, phone);
        return mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private static String day(int offset) {
        return LocalDate.now().plusDays(offset).toString();
    }
}
