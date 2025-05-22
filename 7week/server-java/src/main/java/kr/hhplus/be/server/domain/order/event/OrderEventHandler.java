package kr.hhplus.be.server.domain.order.event;

import kr.hhplus.be.server.domain.event.EventPublisher;
import kr.hhplus.be.server.domain.order.model.DomainOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventHandler {
    
    private final EventPublisher eventPublisher;

    /**
     * 주문 생성 완료 이벤트를 발행
     * @param order 생성된 주문 정보
     */
    public void publishOrderCreated(DomainOrder order) {
        eventPublisher.publish(OrderEvent.created(order));
    }
    
    /**
     * 트랜잭션 완료 후 외부 시스템 전송을 위한 이벤트 처리
     * 이 메소드가 트랜잭션 완료 후 실행되도록 설정
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderEventAfterCommit(OrderEvent event) {
        log.info("트랜잭션 완료 후 주문 이벤트 처리: {}", event.getOrder().getOrderNumber());
        // 여기서 다시 이벤트를 발행하면 비동기 처리되는 ExternalTransmissionService로 전달됨
        eventPublisher.publish(event);
    }
}
