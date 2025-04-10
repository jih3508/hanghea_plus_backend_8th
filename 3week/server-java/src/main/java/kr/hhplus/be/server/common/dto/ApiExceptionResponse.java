package kr.hhplus.be.server.common.dto;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public class ApiExceptionResponse extends RuntimeException{

    private HttpStatus status;
    private String message;

    public ApiExceptionResponse(HttpStatus status, String message){
        super(message);
        this.status = status;
        this.message = message;
    }
}
