package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.model.CreateOrderProductHistory;
import kr.hhplus.be.server.domain.order.model.DomainProductHistory;
import kr.hhplus.be.server.infrastructure.order.entity.OrderProductHistory;
import kr.hhplus.be.server.domain.order.vo.OrderHistoryProductGroupVo;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface OrderProductHistoryRepository {

    DomainProductHistory create(CreateOrderProductHistory create);

    // 각 상품 groupBy 절로 가져온다., 총 수량 순 내림 차순으로 가져온다.
    List<OrderHistoryProductGroupVo> findGroupByProductIdThreeDays();
}
