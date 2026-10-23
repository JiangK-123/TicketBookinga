package com.park.ticket.web;

import com.park.ticket.dto.AttractionView;
import com.park.ticket.dto.AvailabilityView;
import com.park.ticket.dto.CategoryView;
import com.park.ticket.service.AttractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/api/attractions", produces = "application/json")
@Tag(name = "游乐项目")
public class AttractionController {

    private final AttractionService attractions;

    public AttractionController(AttractionService attractions) {
        this.attractions = attractions;
    }

    @GetMapping("/categories")
    @Operation(summary = "项目分类", description = "返回全部分类编码和中文名称，供筛选使用。")
    public List<CategoryView> categories() {
        return attractions.categories();
    }

    @GetMapping
    @Operation(summary = "项目列表", description = "category 精确匹配分类编码；keyword 按项目名称包含匹配，忽略大小写。都不传则返回全部。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "项目列表"),
            @ApiResponse(responseCode = "400", description = "category 不是已知分类")
    })
    public List<AttractionView> list(
            @Parameter(description = "THRILL、FAMILY、WATER、SHOW")
            @RequestParam(required = false) String category,
            @Parameter(description = "项目名称关键字")
            @RequestParam(required = false) String keyword) {
        return attractions.list(category, keyword);
    }

    @GetMapping("/{id}")
    @Operation(summary = "项目详情")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "项目详情"),
            @ApiResponse(responseCode = "400", description = "id 不是数字"),
            @ApiResponse(responseCode = "404", description = "游乐项目不存在")
    })
    public AttractionView get(@PathVariable Long id) {
        return attractions.get(id);
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "查询某日余票", description = "date 必填，格式 yyyy-MM-dd。过去的日期和超过今天之后 30 天的日期都会拒绝。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "当日余票"),
            @ApiResponse(responseCode = "400", description = "日期缺失、格式错误或超出可订范围"),
            @ApiResponse(responseCode = "404", description = "游乐项目不存在")
    })
    public AvailabilityView availability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attractions.availability(id, date);
    }
}
