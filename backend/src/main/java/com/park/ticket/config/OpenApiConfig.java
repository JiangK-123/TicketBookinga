package com.park.ticket.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI parkOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("星河游乐场订票 API")
                        .version("1.0.0")
                        .description("""
                                前后端分离的订票接口。金额单位为元，日期格式 yyyy-MM-dd，时间为本地时间且不带时区。
                                订单状态：UNPAID 待支付，PAID 已支付，CANCELLED 已取消。
                                仅 UNPAID 可以支付或取消；取消会退回当日余票。单笔 1 到 10 张，可订今天起 30 天内（含第 30 天）。
                                创建订单时未知 JSON 字段会被忽略。字段校验失败时 details 给出 field 与 message。
                                """))
                .servers(List.of(new Server().url("http://localhost:8080").description("本地后端")));
    }
}
