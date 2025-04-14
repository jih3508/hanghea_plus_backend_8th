package kr.hhplus.be.server.application.point;

import kr.hhplus.be.server.interfaces.api.point.request.ChargeRequest;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PointChargeCommand {

    private Long userID;

    private BigDecimal amount;

    @Builder
    public PointChargeCommand(Long userID, BigDecimal amount) {
        this.userID = userID;
        this.amount = amount;
    }

    public static PointChargeCommand of(Long userID, ChargeRequest request) {
        return PointChargeCommand.builder()
                .userID(userID)
                .amount(request.getAmount()).build();
    }

}
