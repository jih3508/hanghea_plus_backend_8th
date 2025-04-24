package kr.hhplus.be.server.interfaces.api.point.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class ChargeRequest {

    @NotNull(message = "충전 금액은 필수 값 입니다.")
    @Positive(message = "충전 금액은 양수 입니다.")
    private BigDecimal amount;
}
