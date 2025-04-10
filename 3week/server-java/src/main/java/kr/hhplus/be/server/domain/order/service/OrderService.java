package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
import kr.hhplus.be.server.domain.order.entity.OrderProductHistory;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderProductHistoryRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.user.dto.OrderInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;

    private final OrderItemRepository itemRepository;

    private final OrderProductHistoryRepository historyRepository;


    public Order save(Order order) {
        return repository.save(order);
    }


    public OrderInfoDto save(Order order, List<OrderItem> orderItems) {

        orderItems.stream().forEach(item -> {
            item.setOrder(order);
        });

        itemRepository.manySave(orderItems);

        // history 저장
        orderItems.stream().forEach(item -> {
            OrderProductHistory history = OrderProductHistory.builder()
                    .order(order)
                    .product(item.getProduct())
                    .quantity(item.getQuantity())
                    .build();
            historyRepository.save(history);
        });

        return OrderInfoDto
                .builder()
                .order(order)
                .items(orderItems)
                .build();
    }

    /*
     * method: createOrderNumber
     * description: 주문 번호 생성
     */
    public String createOrderNumber(){
        LocalDateTime now = LocalDateTime.now();
        return String.format("{0}{1}{2}%08d", now.getYear(), now.getMonth(), now.getDayOfMonth(), (int)(Math.random() * 1_000_000_000) + 1);
    }

}
