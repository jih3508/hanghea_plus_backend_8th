package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.domain.order.model.DomainOrder;
import kr.hhplus.be.server.domain.order.model.OrderStatus;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDomainService {
    
    private final OrderRepository orderRepository;
    
    /**
     * 주문 완료 처리
     */
    @Transactional
    public void completeOrder(Long orderId) {
        DomainOrder order = orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId);
        }
        
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
        
        log.info("주문 완료 처리됨 - orderId: {}, userId: {}", orderId, order.getUserId());
    }
    
    /**
     * 주문 취소 처리
     */
    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        DomainOrder order = orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId);
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        orderRepository.save(order);
        
        log.info("주문 취소 처리됨 - orderId: {}, userId: {}, reason: {}", orderId, order.getUserId(), reason);
    }
    
    /**
     * 주문 조회
     */
    public DomainOrder getOrder(Long orderId) {
        DomainOrder order = orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId);
        }
        return order;
    }
}
