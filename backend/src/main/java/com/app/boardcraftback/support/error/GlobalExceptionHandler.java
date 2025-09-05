package com.app.boardcraftback.support.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static com.app.boardcraftback.support.web.TraceIdFilter.TRACE_ID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400: @Valid 바디 검증 실패 (JSON)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fields = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.put(fe.getField(), fe.getDefaultMessage());
        }
        return build(req, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "입력값이 올바르지 않습니다.", fields);
    }

    // 특수상황: 밸리데이션으로 검증 실패가 발생한 것은 아니지만 밸리데이션 익셉션이 발생한 것처럼 발생시켜야 할 때 사용.
    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<ErrorResponse> handleFieldValidation(FieldValidationException ex, HttpServletRequest req) {
        return build(req, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "입력값이 올바르지 않습니다.", ex.getErrors());
    }

    // 비즈니스 예외처리
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleApp(AppException ex, HttpServletRequest req) {
        var code = ex.getCode();
        return build(req, code.getStatus(), code.name(), code.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception ex, HttpServletRequest req) {
        var code = ErrorCode.INTERNAL_ERROR;
        ex.printStackTrace();
        return build(req, code.getStatus(), code.name(), code.getMessage(), null);
    }

    private ResponseEntity<ErrorResponse> build(HttpServletRequest req, HttpStatus status,
                                                String code, String message, Map<String, String> validation) {
        String traceId = String.valueOf(req.getAttribute(TRACE_ID));
        ErrorResponse body = ErrorResponse.of(traceId, req.getRequestURI(), code, message, validation);
        return ResponseEntity.status(status).body(body);
    }
}
