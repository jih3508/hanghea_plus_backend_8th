package kr.hhplus.be.server.domain.point.model;

import kr.hhplus.be.server.infrastructure.point.entity.PointTransactionType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class CreatePointHistory {

    private Long userId;

    private BigDecimal amount;

    private PointTransactionType type;

    public static CreatePointHistory create(Long userId, BigDecimal amount, PointTransactionType type) {
        return  CreatePointHistory.builder()
                .userId(userId)
                .amount(amount)
                .type(type)
                .build();
    }
}
