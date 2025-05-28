package kr.hhplus.be.server.kafka;

import kr.hhplus.be.server.common.event.OrderCreatedEvent;
import kr.hhplus.be.server.infrastructure.kafka.EventPublisher;
import kr.hhplus.be.server.infrastructure.kafka.KafkaTopics;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(
    partitions = 1,
    topics = { KafkaTopics.ORDER_EVENTS },
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9093",
        "port=9093"
    }
)
class KafkaIntegrationTest {

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    void 주문생성이벤트_발행_및_소비_테스트() throws InterruptedException {
        // Given
        OrderCreatedEvent.OrderItem orderItem = new OrderCreatedEvent.OrderItem(
                1L, "Test Product", 2, BigDecimal.valueOf(10000)
        );
        
        OrderCreatedEvent event = new OrderCreatedEvent(
                123L,
                456L,
                BigDecimal.valueOf(20000),
                List.of(orderItem),
                "CREATED"
        );

        // Consumer 설정
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "test-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        DefaultKafkaConsumerFactory<String, OrderCreatedEvent> consumerFactory = 
                new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties(KafkaTopics.ORDER_EVENTS);
        
        BlockingQueue<ConsumerRecord<String, OrderCreatedEvent>> records = new LinkedBlockingQueue<>();
        containerProperties.setMessageListener((MessageListener<String, OrderCreatedEvent>) records::add);

        KafkaMessageListenerContainer<String, OrderCreatedEvent> container =
                new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        // When
        eventPublisher.publishOrderEvent("456", event);

        // Then
        ConsumerRecord<String, OrderCreatedEvent> received = records.poll(10, TimeUnit.SECONDS);
        
        assertThat(received).isNotNull();
        assertThat(received.key()).isEqualTo("456");
        assertThat(received.value().getOrderId()).isEqualTo(123L);
        assertThat(received.value().getUserId()).isEqualTo(456L);
        assertThat(received.value().getTotalAmount()).isEqualTo(BigDecimal.valueOf(20000));
        assertThat(received.value().getOrderItems()).hasSize(1);
        assertThat(received.value().getOrderItems().get(0).getProductId()).isEqualTo(1L);

        container.stop();
    }
}
