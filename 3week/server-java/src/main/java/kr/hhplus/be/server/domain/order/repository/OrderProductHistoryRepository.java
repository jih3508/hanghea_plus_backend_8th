package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.entity.OrderProductHistory;
import kr.hhplus.be.server.domain.order.vo.OrderHistoryProductGroupVo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderProductHistoryRepository {

    OrderProductHistory save(OrderProductHistory orderProductHistory);

    // 각 상품 groupBy 절로 가져온다., 총 수량 순 내림 차순으로 가져온다.
    List<OrderHistoryProductGroupVo> findGroupByProductIdThreeDays();
}
