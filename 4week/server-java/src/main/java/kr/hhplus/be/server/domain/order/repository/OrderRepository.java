package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.model.CreateOrder;
import kr.hhplus.be.server.domain.order.model.DomainOrder;
import kr.hhplus.be.server.infrastructure.order.entity.Order;
import org.springframework.stereotype.Repository;

public interface OrderRepository {

    DomainOrder create(CreateOrder createOrder);
}
