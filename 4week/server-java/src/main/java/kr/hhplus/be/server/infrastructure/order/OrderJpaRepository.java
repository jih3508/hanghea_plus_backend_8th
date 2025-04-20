package kr.hhplus.be.server.infrastructure.order;

import kr.hhplus.be.server.infrastructure.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByUserId(Long userId);
}
