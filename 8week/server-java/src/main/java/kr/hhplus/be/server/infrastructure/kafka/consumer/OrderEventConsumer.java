package kr.hhplus.be.server.infrastructure.kafka.consumer;

import kr.hhplus.be.server.common.event.OrderCompletedEvent;
import kr.hhplus.be.server.common.event.OrderCreatedEvent;
import kr.hhplus.be.server.infrastructure.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    /**
     * 주문 생성 이벤트 처리
     */
    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "order-analytics-group")
    public void handleOrderCreatedEvent(
            @Payload OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Processing OrderCreatedEvent - Topic: {}, Partition: {}, Offset: {}, OrderId: {}, UserId: {}", 
                    topic, partition, offset, event.getOrderId(), event.getUserId());

            // 주문 분석 데이터 처리
            processOrderAnalytics(event);
            
            // 재고 감소 이벤트 발행 (다른 서비스에서 처리)
            // inventoryService.processInventoryDeduction(event);
            
            // 수동 커밋
            acknowledgment.acknowledge();
            
            log.info("Successfully processed OrderCreatedEvent - OrderId: {}", event.getOrderId());
            
        } catch (Exception e) {
            log.error("Failed to process OrderCreatedEvent - OrderId: {}, Error: {}", 
                    event.getOrderId(), e.getMessage(), e);
            // 에러 처리 로직 (재시도, DLQ 등)
        }
    }

    /**
     * 주문 완료 이벤트 처리
     */
    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "order-notification-group")
    public void handleOrderCompletedEvent(
            @Payload OrderCompletedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Processing OrderCompletedEvent - Topic: {}, Partition: {}, Offset: {}, OrderId: {}, UserId: {}", 
                    topic, partition, offset, event.getOrderId(), event.getUserId());

            // 주문 완료 알림 처리
            processOrderCompletionNotification(event);
            
            // 고객 포인트 적립 처리
            processLoyaltyPointsAccrual(event);
            
            // 수동 커밋
            acknowledgment.acknowledge();
            
            log.info("Successfully processed OrderCompletedEvent - OrderId: {}", event.getOrderId());
            
        } catch (Exception e) {
            log.error("Failed to process OrderCompletedEvent - OrderId: {}, Error: {}", 
                    event.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * 주문 분석 데이터 처리
     */
    private void processOrderAnalytics(OrderCreatedEvent event) {
        log.info("Processing order analytics for OrderId: {}", event.getOrderId());
        
        // 실시간 매출 대시보드 업데이트
        // dashboardService.updateRealTimeSales(event);
        
        // 상품 판매량 집계
        // productAnalyticsService.updateProductSalesCount(event.getOrderItems());
        
        // 사용자 구매 패턴 분석
        // userBehaviorService.analyzeUserPurchasePattern(event.getUserId(), event.getOrderItems());
    }

    /**
     * 주문 완료 알림 처리
     */
    private void processOrderCompletionNotification(OrderCompletedEvent event) {
        log.info("Processing order completion notification for OrderId: {}", event.getOrderId());
        
        // 주문 완료 이메일 발송
        // emailService.sendOrderCompletionEmail(event.getUserId(), event.getOrderId());
        
        // 주문 완료 푸시 알림
        // pushNotificationService.sendOrderCompletionPush(event.getUserId(), event.getOrderId());
    }

    /**
     * 고객 포인트 적립 처리
     */
    private void processLoyaltyPointsAccrual(OrderCompletedEvent event) {
        log.info("Processing loyalty points accrual for OrderId: {}", event.getOrderId());
        
        // 주문 금액의 1% 포인트 적립
        // loyaltyService.accruePoints(event.getUserId(), event.getTotalAmount().multiply(BigDecimal.valueOf(0.01)));
    }
}
