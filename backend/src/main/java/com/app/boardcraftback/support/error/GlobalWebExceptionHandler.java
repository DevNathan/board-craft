package com.app.boardcraftback.support.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

import static com.app.boardcraftback.support.web.TraceIdFilter.TRACE_ID;

@ControllerAdvice
public class GlobalWebExceptionHandler {

    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<ErrorResponse> handleFieldValidation(FieldValidationException ex, HttpServletRequest req) {
        return build(req, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "입력값이 올바르지 않습니다.", ex.getErrors());
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public String handle404(Exception ex, Model model, HttpServletRequest req) {
        model.addAttribute("status", 404);
        model.addAttribute("error", "NOT_FOUND");
        model.addAttribute("message", "요청한 페이지를 찾을 수 없습니다.");
        model.addAttribute("path", req.getRequestURI());
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handle500(Exception ex, Model model, HttpServletRequest req) {
        model.addAttribute("status", 500);
        model.addAttribute("error", "INTERNAL_SERVER_ERROR");
        model.addAttribute("message", "서버 내부 오류가 발생했습니다.");
        model.addAttribute("path", req.getRequestURI());
        model.addAttribute("TRACE_ID", req.getAttribute("TRACE_ID"));
        return "error/500";
    }

    private ResponseEntity<ErrorResponse> build(HttpServletRequest req, HttpStatus status,
                                                String code, String message, Map<String, String> validation) {
        String traceId = String.valueOf(req.getAttribute(TRACE_ID));
        ErrorResponse body = ErrorResponse.of(traceId, req.getRequestURI(), code, message, validation);
        return ResponseEntity.status(status).body(body);
    }
}
