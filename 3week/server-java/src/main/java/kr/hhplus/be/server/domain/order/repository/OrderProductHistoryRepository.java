package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.entity.OrderProductHistory;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderProductHistoryRepository {

    OrderProductHistory save(OrderProductHistory orderProductHistory);
}
