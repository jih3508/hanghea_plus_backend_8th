package kr.hhplus.be.server.domain.point.model;

import kr.hhplus.be.server.infrastructure.point.entity.PointTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@Builder
public class DomainPointHistory {

    private Long id;

    private Long userId;

    private PointTransactionType type;

    private BigDecimal amount;

    private LocalDateTime createdDateTime; // 생성 일시

}
