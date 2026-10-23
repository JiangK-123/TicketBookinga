package com.park.ticket.web;

import com.park.ticket.api.ApiError;
import com.park.ticket.api.ApiException;
import com.park.ticket.api.FieldViolation;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApi(ApiException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiError.of(ex.getCode(), ex.getMessage(), ex.getDetails(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldViolation> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
                .toList();
        return badRequest(request, "VALIDATION_ERROR", "请求参数不合法", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return badRequest(
                request,
                "MALFORMED_JSON",
                "请求体无法解析。请提交 JSON，日期使用 yyyy-MM-dd，数量使用整数",
                List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissing(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String name = ex.getParameterName();
        String message = "date".equals(name)
                ? "缺少参数 date，格式应为 yyyy-MM-dd"
                : "缺少参数 " + name;
        return badRequest(request, "MISSING_PARAMETER", message, List.of(new FieldViolation(name, message)));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleType(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String name = ex.getName() == null ? "parameter" : ex.getName();
        String message = switch (name) {
            case "date" -> "参数 date 格式应为 yyyy-MM-dd";
            case "id" -> "参数 id 必须是数字";
            default -> "参数 " + name + " 类型不正确";
        };
        return badRequest(request, "TYPE_MISMATCH", message, List.of(new FieldViolation(name, message)));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMedia(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiError.of("UNSUPPORTED_MEDIA", "请使用 Content-Type: application/json", List.of(), request.getRequestURI()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethod(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiError.of("METHOD_NOT_ALLOWED", "不支持该请求方法", List.of(), request.getRequestURI()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        if (request.getRequestURI() != null && request.getRequestURI().startsWith("/api/")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiError.of("NOT_FOUND", "接口不存在", List.of(), request.getRequestURI()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknown(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error on {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of("INTERNAL_ERROR", "服务暂时不可用", List.of(), request.getRequestURI()));
    }

    private ResponseEntity<ApiError> badRequest(HttpServletRequest request, String code, String message, List<FieldViolation> details) {
        return ResponseEntity.badRequest().body(ApiError.of(code, message, details, request.getRequestURI()));
    }
}
