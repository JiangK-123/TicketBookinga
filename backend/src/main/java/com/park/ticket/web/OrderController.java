package com.park.ticket.web;

import com.park.ticket.dto.CreateOrderRequest;
import com.park.ticket.dto.OrderView;
import com.park.ticket.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping(value = "/api/orders", produces = "application/json")
@Tag(name = "订单")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    @Operation(summary = "创建订单", description = "按下单数量占用当日余票。仅待支付订单占用余票。")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "已创建，Location 指向订单详情"),
            @ApiResponse(responseCode = "400", description = "字段不合法、日期超范围或 JSON 无法解析"),
            @ApiResponse(responseCode = "404", description = "游乐项目不存在"),
            @ApiResponse(responseCode = "409", description = "余票不足，code 为 SOLD_OUT")
    })
    public ResponseEntity<OrderView> create(@Valid @RequestBody CreateOrderRequest request, UriComponentsBuilder uriBuilder) {
        OrderView order = orders.create(request);
        return ResponseEntity.created(uriBuilder.path("/api/orders/{orderNo}").build(order.orderNo())).body(order);
    }

    @GetMapping
    @Operation(summary = "按手机号查询订单", description = "phone 必填，精确匹配下单手机号。没有订单时返回空数组。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "订单列表，没有订单时为空数组"),
            @ApiResponse(responseCode = "400", description = "缺少手机号或格式不正确")
    })
    public List<OrderView> list(@RequestParam(required = false) String phone) {
        return orders.listByPhone(phone);
    }

    @GetMapping("/{orderNo}")
    @Operation(summary = "订单详情")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "订单详情"),
            @ApiResponse(responseCode = "404", description = "订单不存在")
    })
    public OrderView get(@PathVariable String orderNo) {
        return orders.get(orderNo);
    }

    @PostMapping("/{orderNo}/pay")
    @Operation(summary = "支付订单", description = "只有 UNPAID 可以支付，成功后状态变为 PAID。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "已支付"),
            @ApiResponse(responseCode = "404", description = "订单不存在"),
            @ApiResponse(responseCode = "409", description = "当前状态不能支付，code 为 ILLEGAL_STATE")
    })
    public OrderView pay(@PathVariable String orderNo) {
        return orders.pay(orderNo);
    }

    @PostMapping("/{orderNo}/cancel")
    @Operation(summary = "取消订单", description = "只有 UNPAID 可以取消。取消后状态变为 CANCELLED，并退回当日余票。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "已取消"),
            @ApiResponse(responseCode = "404", description = "订单不存在"),
            @ApiResponse(responseCode = "409", description = "当前状态不能取消，code 为 ILLEGAL_STATE")
    })
    public OrderView cancel(@PathVariable String orderNo) {
        return orders.cancel(orderNo);
    }
}
