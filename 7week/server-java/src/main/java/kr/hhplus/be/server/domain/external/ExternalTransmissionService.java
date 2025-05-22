package kr.hhplus.be.server.domain.external;

import kr.hhplus.be.server.domain.order.event.OrderEvent;
import kr.hhplus.be.server.domain.order.model.DomainOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExternalTransmissionService {

    /**
     * 비동기로 주문 데이터를 외부 데이터 플랫폼으로 전송
     * @param event 주문 이벤트
     */
    @Async
    @EventListener
    public void handleOrderEvent(OrderEvent event) {
        try {
            log.info("외부 데이터 플랫폼으로 주문 데이터 전송 시작: {}", event.getOrder().getOrderNumber());
            // 실제 전송 구현
            sendOrderData(event.getOrder());
            log.info("외부 데이터 플랫폼으로 주문 데이터 전송 완료: {}", event.getOrder().getOrderNumber());
        } catch (Exception e) {
            // 외부 전송 실패가 주문 로직에 영향을 주지 않도록 예외 처리
            log.error("외부 데이터 플랫폼으로 주문 데이터 전송 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 실제 외부 데이터 플랫폼으로 주문 데이터 전송 로직
     * @param order 주문 정보
     */
    private void sendOrderData(DomainOrder order) {
        // 외부 API 호출 또는 메시지 큐에 데이터 전송 로직 구현
        // 여기서는 Mock 구현으로 대체
        log.info("주문 데이터 전송 - 주문번호: {}, 금액: {}", order.getOrderNumber(), order.getTotalPrice());
    }
}
