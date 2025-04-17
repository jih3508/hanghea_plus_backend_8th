package kr.hhplus.be.server.domain.point.model;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class DomainPoint {

    private static final BigDecimal MAX_POINT = new BigDecimal(1_000_000_000L);

    private Long id;

    private Long userId;

    private BigDecimal point;

    public void charge(BigDecimal amount) {
        this.point = this.point.add(amount);

        if(point.compareTo(MAX_POINT) > 0) {
            throw new ApiExceptionResponse(HttpStatus.BAD_REQUEST, "충전후 포인트가 한도 초과 되었습니다.");
        }
    }


    public void use(BigDecimal amount) {
        if(point.compareTo(amount) < 0) {
            throw new ApiExceptionResponse(HttpStatus.BAD_REQUEST, "잔액 부족!!!!");
        }

        this.point = this.point.subtract(amount);
    }
}
