package kr.hhplus.be.server.common.event.order;

import kr.hhplus.be.server.domain.external.ExternalTransmissionService;
import kr.hhplus.be.server.domain.order.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalDataTransmissionProcessor {

    private final ExternalTransmissionService externalTransmissionService;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            autoCreateTopics = "true",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(
            topics = "order-events",
            groupId = "order-processing-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderEvent(
            @Payload OrderEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Received order event: topic={}, key={}, offset={}", topic, key, offset);

        try {
            boolean isSuccess = externalTransmissionService.sendOrderData(event.getOrder());
            if (isSuccess) {
                log.info("외부 데이터 전송 성공\n 외부 데이터: {}", event.getOrder());
            } else {
                log.error("외부 데이터 전송 실패\n 외부 데이터: {}", event.getOrder());
                throw new RuntimeException("External transmission failed"); // 재시도를 위해
            }
        } catch (Exception e) {
            log.error("Error processing order event", e);
            throw e; // 재시도 메커니즘 활용
        }
    }
}