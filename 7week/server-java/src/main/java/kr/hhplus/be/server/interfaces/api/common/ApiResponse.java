package kr.hhplus.be.server.interfaces.api.common;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {


    private T data;

    private HttpStatus status;

    public ApiResponse(HttpStatus status, T data) {
        this.status = status;
        this.data = data;
    }

    public static ApiResponse<String> create(){
        return ApiResponse.<String>builder()
                .data( "success create!!")
                .status(HttpStatus.CREATED)
                .build();
    }

    public static <T> ApiResponse<T> create(T data){
        return ApiResponse.<T>builder()
                .data(data)
                .status(HttpStatus.CREATED)
                .build();
    }

    public static ApiResponse<String> ok(){
        return ApiResponse.<String>builder()
                .data( "success!!")
                .status(HttpStatus.OK)
                .build();
    }

    public static <T> ApiResponse<T> ok(T data){
        return ApiResponse.<T>builder()
                .data(data)
                .status(HttpStatus.OK)
                .build();
    }


    public static <T> ApiResponse<T> error(T data, HttpStatus httpStatus) {
        return ApiResponse.<T>builder()
                .data(data)
                .status(httpStatus)
                .build();
    }

    public static ApiResponse<String> error(String errors, HttpStatus httpStatus) {
        return ApiResponse.<String>builder()
                .data( errors)
                .status(httpStatus)
                .build();
    }

    public static ApiResponse<String> error(String errors){
        return ApiResponse.<String>builder()
                .data( errors)
                .status(HttpStatus.BAD_REQUEST)
                .build();
    }
}
