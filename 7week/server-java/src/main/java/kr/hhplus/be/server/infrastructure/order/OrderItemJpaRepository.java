package kr.hhplus.be.server.infrastructure.order;

import kr.hhplus.be.server.infrastructure.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {
}
