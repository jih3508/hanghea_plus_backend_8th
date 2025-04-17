package kr.hhplus.be.server.infrastructure.order;

import kr.hhplus.be.server.infrastructure.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {
}
