package kr.hhplus.be.server.domain.point.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class UpdatePoint {

    private Long pointId;

    private BigDecimal point;
}
