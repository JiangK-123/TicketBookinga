package com.park.ticket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 跨域只在这里配置，控制器上不再使用 {@code @CrossOrigin}。
 * 前端通过 Axios 直连 http://localhost:8080，不经过 Vite 代理，这样浏览器会真正发起跨域请求。
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = corsProperties.getAllowedOrigins();
        if (origins == null || origins.isEmpty()) {
            throw new IllegalStateException("app.cors.allowed-origins 不能为空");
        }
        registry.addMapping("/api/**")
                .allowedOrigins(origins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Location")
                .maxAge(3600);
    }
}
