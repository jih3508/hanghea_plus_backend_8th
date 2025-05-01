package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.model.CreateOrder;
import kr.hhplus.be.server.domain.order.model.DomainOrder;
import kr.hhplus.be.server.infrastructure.order.entity.Order;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface OrderRepository {

    DomainOrder create(CreateOrder createOrder);

    List<DomainOrder> findByUserId(Long userId);
    
    DomainOrder findById(Long orderId);
    
    void updateStatus(Long orderId, String status);
}
