package com.park.ticket.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "失败时的响应体。2xx 不会使用这个结构。")
public record ApiError(
        String code,
        String message,
        List<FieldViolation> details,
        String path,
        String timestamp) {

    public static ApiError of(String code, String message, List<FieldViolation> details, String path) {
        List<FieldViolation> safeDetails = details == null ? List.of() : List.copyOf(details);
        return new ApiError(code, message, safeDetails, path, OffsetDateTime.now().toString());
    }
}
