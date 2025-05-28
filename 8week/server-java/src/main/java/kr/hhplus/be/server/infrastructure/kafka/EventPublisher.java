package kr.hhplus.be.server.infrastructure.kafka;

import kr.hhplus.be.server.common.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 이벤트를 Kafka 토픽에 발행
     * @param topic 토픽 이름
     * @param key 파티션 키 (동일한 키는 같은 파티션으로 전송)
     * @param event 이벤트 객체
     */
    public void publishEvent(String topic, String key, BaseEvent event) {
        try {
            log.info("Publishing event to topic: {}, key: {}, eventType: {}, eventId: {}", 
                    topic, key, event.getEventType(), event.getEventId());
            
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(topic, key, event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Event published successfully - Topic: {}, Partition: {}, Offset: {}, EventId: {}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            event.getEventId());
                } else {
                    log.error("Failed to publish event - Topic: {}, EventId: {}, Error: {}",
                            topic, event.getEventId(), ex.getMessage(), ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Exception occurred while publishing event - Topic: {}, EventId: {}, Error: {}",
                    topic, event.getEventId(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    /**
     * 주문 이벤트 발행
     */
    public void publishOrderEvent(String key, BaseEvent event) {
        publishEvent(KafkaTopics.ORDER_EVENTS, key, event);
    }

    /**
     * 결제 이벤트 발행
     */
    public void publishPaymentEvent(String key, BaseEvent event) {
        publishEvent(KafkaTopics.PAYMENT_EVENTS, key, event);
    }

    /**
     * 재고 이벤트 발행
     */
    public void publishInventoryEvent(String key, BaseEvent event) {
        publishEvent(KafkaTopics.INVENTORY_EVENTS, key, event);
    }

    /**
     * 알림 이벤트 발행
     */
    public void publishNotificationEvent(String key, BaseEvent event) {
        publishEvent(KafkaTopics.NOTIFICATION_EVENTS, key, event);
    }
}
