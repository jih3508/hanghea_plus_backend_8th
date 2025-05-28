package kr.hhplus.be.server.infrastructure.kafka.consumer;

import kr.hhplus.be.server.common.event.BaseEvent;
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

/**
 * 데이터 플랫폼으로 전송하는 컨슈머
 * 모든 이벤트를 수집하여 데이터 웨어하우스나 분석 시스템으로 전송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataPlatformConsumer {

    /**
     * 모든 주문 이벤트를 데이터 플랫폼으로 전송
     */
    @KafkaListener(topics = {
        KafkaTopics.ORDER_EVENTS,
        KafkaTopics.PAYMENT_EVENTS,
        KafkaTopics.INVENTORY_EVENTS
    }, groupId = "data-platform-group")
    public void handleAllEvents(
            @Payload BaseEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Sending event to data platform - Topic: {}, Partition: {}, Offset: {}, EventType: {}, EventId: {}", 
                    topic, partition, offset, event.getEventType(), event.getEventId());

            // 데이터 플랫폼으로 이벤트 전송
            sendToDataPlatform(event, topic);
            
            // 수동 커밋
            acknowledgment.acknowledge();
            
            log.info("Successfully sent event to data platform - EventId: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to send event to data platform - EventId: {}, Error: {}", 
                    event.getEventId(), e.getMessage(), e);
            // 실패한 이벤트는 재시도 또는 별도 저장소에 보관
        }
    }

    /**
     * 데이터 플랫폼으로 이벤트 전송
     */
    private void sendToDataPlatform(BaseEvent event, String sourceTopic) {
        // 실제 구현에서는 다음과 같은 방식으로 데이터 플랫폼에 전송
        // 1. REST API 호출
        // 2. 다른 메시지 큐 (RabbitMQ, Amazon SQS 등)로 전송
        // 3. 데이터베이스에 직접 저장
        // 4. 파일 시스템에 저장 (배치 처리용)
        
        log.info("Simulating data platform transmission - EventType: {}, Source: {}", 
                event.getEventType(), sourceTopic);
        
        // 예시: JSON 형태로 변환하여 외부 시스템에 전송
        // String jsonPayload = objectMapper.writeValueAsString(event);
        // dataLakeService.sendEvent(jsonPayload, event.getEventType());
        
        // 예시: 실시간 분석을 위해 스트리밍 플랫폼에 전송
        // streamingService.publishToAnalytics(event);
        
        // 예시: 데이터 웨어하우스에 배치 적재를 위해 임시 저장
        // stagingService.stageEventForBatchProcessing(event);
    }

    /**
     * 특정 이벤트 타입별 전용 처리
     */
    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "order-data-warehouse-group")
    public void handleOrderEventsForDW(
            @Payload Object eventPayload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            if (eventPayload instanceof OrderCreatedEvent orderEvent) {
                processOrderCreatedForDW(orderEvent);
            } else if (eventPayload instanceof OrderCompletedEvent orderEvent) {
                processOrderCompletedForDW(orderEvent);
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process order event for DW - Error: {}", e.getMessage(), e);
        }
    }

    private void processOrderCreatedForDW(OrderCreatedEvent event) {
        log.info("Processing OrderCreatedEvent for Data Warehouse - OrderId: {}", event.getOrderId());
        
        // 주문 생성 데이터를 데이터 웨어하우스 형태로 변환
        // OrderDWRecord dwRecord = OrderDWRecord.from(event);
        // dataWarehouseService.insertOrderRecord(dwRecord);
    }

    private void processOrderCompletedForDW(OrderCompletedEvent event) {
        log.info("Processing OrderCompletedEvent for Data Warehouse - OrderId: {}", event.getOrderId());
        
        // 주문 완료 데이터를 데이터 웨어하우스에 업데이트
        // dataWarehouseService.updateOrderCompletion(event.getOrderId(), event.getCompletedAt());
    }
}
