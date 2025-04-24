package kr.hhplus.be.server.interfaces.api.point.response;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class PointResponse {

    @NotNull
    private BigDecimal amount;

}
