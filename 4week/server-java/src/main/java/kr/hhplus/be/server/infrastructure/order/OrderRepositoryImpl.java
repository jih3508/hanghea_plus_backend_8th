package kr.hhplus.be.server.infrastructure.order;

import kr.hhplus.be.server.domain.order.model.CreateOrder;
import kr.hhplus.be.server.domain.order.model.DomainOrder;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.infrastructure.order.entity.Order;
import kr.hhplus.be.server.infrastructure.order.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository repository;

    private final OrderItemJpaRepository itemJpaRepository;

    @Override
    public DomainOrder create(CreateOrder createOrder) {
        DomainOrder order = repository.save(Order.create(createOrder)).toDomain();

        // 주문 항목 추가
        createOrder.getOrderItems().forEach(item -> {
            order.addItem(itemJpaRepository.save(OrderItem.create(order.getId(), item)).toDomain());
        });

        return order;
    }
}
