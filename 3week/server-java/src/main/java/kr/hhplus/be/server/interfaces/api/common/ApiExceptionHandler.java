package kr.hhplus.be.server.interfaces.api.common;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiExceptionResponse.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleApiExceptionResponse(ApiExceptionResponse response) {
        return response;
    }

}
