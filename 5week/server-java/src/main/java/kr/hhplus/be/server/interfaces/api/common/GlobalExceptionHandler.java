package kr.hhplus.be.server.interfaces.api.common;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 모든 API 요청의 예외를 처리하는 글로벌 예외 처리기
 * 일관된 형식의 응답을 반환하여 클라이언트가 쉽게 처리할 수 있도록 함
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * API 비즈니스 로직 예외 처리
     */
    @ExceptionHandler(ApiExceptionResponse.class)
    public ResponseEntity<ApiResponse<String>> handleApiExceptionResponse(ApiExceptionResponse ex) {
        log.error("API 예외 발생: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), ex.getStatus());
    }

    /**
     * 요청 바디 유효성 검증 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ApiResponse.error(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * 폼 데이터 바인딩 예외 처리
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleBindExceptions(BindException ex) {
        Map<String, String> errors = ex.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing + ", " + replacement
                ));
        return ApiResponse.error(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * 제약 조건 위반 예외 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
        });
        return ApiResponse.error(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * 기타 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<String> handleGeneralExceptions(Exception ex) {
        log.error("예상치 못한 오류 발생: {}", ex.getMessage(), ex);
        return ApiResponse.error("서버에서 예상치 못한 오류가 발생했습니다. 나중에 다시 시도해 주세요.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}