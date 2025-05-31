package kr.hhplus.be.server.common.event.order;

import kr.hhplus.be.server.common.kafka.TopicType;
import kr.hhplus.be.server.domain.order.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalDataTransmissionPublisher {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void sendOrderEvent(OrderEvent event) {
        String key = event.getOrder().getOrderNumber(); // 주문번호를 키로 사용

        CompletableFuture<SendResult<String, OrderEvent>> future =
                kafkaTemplate.send(TopicType.ORDER.getTopic(), key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Order event sent successfully: key={}, offset={}",
                        key, result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send order event: key={}", key, ex);
                // 실패 시 처리 로직 (재시도, DLQ 등)
            }
        });
    }
}