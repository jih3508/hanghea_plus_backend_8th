package kr.hhplus.be.server.domain.order.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrderHistoryProductGroupVo {

    private Long productId;

    private Integer totalQuantity;
}
