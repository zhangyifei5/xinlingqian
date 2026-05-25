package com.example.xinlingqian.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.xinlingqian.util.ResponseUtil;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseUtil<Void>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(ResponseUtil.badRequest(e.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseUtil<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ResponseUtil<Map<String, String>> response = new ResponseUtil<>(400, "参数校验失败", errors);
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ResponseUtil<Void>> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        return ResponseEntity.badRequest().body(ResponseUtil.badRequest("缺少必要的请求头: " + e.getHeaderName()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseUtil<Void>> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseUtil.error("服务器内部错误: " + e.getMessage()));
    }
}
