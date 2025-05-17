package kr.hhplus.be.server.interfaces.api.point.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class ChargeRequest {

    @NotNull(message = "충전 금액은 필수 값 입니다.")
    private BigDecimal amount;

    public ChargeRequest(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiExceptionResponse(HttpStatus.BAD_REQUEST, "충전 금액은 양수 입니다.");
        }
        this.amount = amount;
    }
}
