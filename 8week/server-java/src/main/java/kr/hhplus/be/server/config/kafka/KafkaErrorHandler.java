package kr.hhplus.be.server.config.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;

@Slf4j
public class KafkaErrorHandler implements CommonErrorHandler {

    @Override
    public boolean handleOne(Exception thrownException, ConsumerRecord<?, ?> record,
                           Consumer<?, ?> consumer, MessageListenerContainer container) {
        log.error("Kafka message processing failed. Topic: {}, Partition: {}, Offset: {}, Error: {}",
                record.topic(), record.partition(), record.offset(), thrownException.getMessage(), thrownException);
        
        // 특정 예외에 대한 처리 로직
        if (thrownException instanceof IllegalArgumentException) {
            log.warn("Invalid message format detected. Skipping message: {}", record.value());
            // 메시지를 스킵하고 다음 메시지 처리
            return true;
        }
        
        // 재시도 로직이나 DLQ(Dead Letter Queue) 전송 로직을 여기에 추가할 수 있습니다.
        // 현재는 로그만 남기고 메시지를 스킵합니다.
        return true;
    }

    @Override
    public void handleOtherException(Exception thrownException, Consumer<?, ?> consumer,
                                   MessageListenerContainer container, boolean batchListener) {
        log.error("Unexpected error in Kafka consumer: {}", thrownException.getMessage(), thrownException);
    }
}
