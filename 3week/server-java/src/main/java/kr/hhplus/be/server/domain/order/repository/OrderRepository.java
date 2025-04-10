package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.entity.Order;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository {

    Order save(Order order);
}
