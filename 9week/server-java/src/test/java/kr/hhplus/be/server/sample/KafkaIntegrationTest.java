package kr.hhplus.be.server.sample;

import kr.hhplus.be.server.support.KafkaTestContainersConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${kafka.bootstrap-servers}"
})
@ActiveProfiles("test")
public class KafkaIntegrationTest {

    private static final String TEST_TOPIC = "test-topic";
    private static final Logger log = LoggerFactory.getLogger(KafkaIntegrationTest.class);
    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;
    private KafkaTestContainersConfig kafkaConfig;

    @BeforeEach
    void setUp() {
        kafkaConfig = new KafkaTestContainersConfig();

        // Producer 설정
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                KafkaTestContainersConfig.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        producerProps.put(ProducerConfig.ACKS_CONFIG, "all");

        producer = new KafkaProducer<>(producerProps);

        // Consumer 설정
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                KafkaTestContainersConfig.getBootstrapServers());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(TEST_TOPIC));
    }

    @AfterEach
    void tearDown() {
        if (producer != null) {
            producer.close();
        }
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void 어드민_클라이언트_동작() {
        // Given & When
        var adminClient = kafkaConfig.getAdminClient();

        // Then
        assertThat(adminClient).isNotNull();

        // Clean up
        adminClient.close();
    }

    @Test
    void 카프카_프로듀서_컨슈머_간단_테스트() throws ExecutionException, InterruptedException {
        // Given
        String testKey = "test-key";
        String testMessage = "Hello Kafka!";

        // When - 메시지 전송
        ProducerRecord<String, String> record = new ProducerRecord<>(TEST_TOPIC, testKey, testMessage);
        Future<RecordMetadata> future = producer.send(record);
        RecordMetadata metadata = future.get();

        // Then - 전송 확인
        assertNotNull(metadata);
        assertEquals(TEST_TOPIC, metadata.topic());
        assertTrue(metadata.offset() >= 0);

        // When - 메시지 수신
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

        // Then - 수신 확인
        assertThat(records).isNotNull();
        ConsumerRecord<String, String> receivedRecord = records.iterator().next();
        assertThat(testKey).isEqualTo(receivedRecord.key());
        assertThat(testMessage).isEqualTo(receivedRecord.value());
        assertThat(TEST_TOPIC).isEqualTo(receivedRecord.topic());

        log.info("Received - Key: {}, Value: {}, Partition: {}, Offset: {}",
                receivedRecord.key(), receivedRecord.value(), receivedRecord.partition(), receivedRecord.offset());
    }

    @Test
    void 다중_메세지_테스트() throws ExecutionException, InterruptedException {
        // Given
        int messageCount = 5;

        // When - 여러 메시지 전송
        for (int i = 0; i < messageCount; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    TEST_TOPIC,
                    "key-" + i,
                    "message-" + i
            );
            producer.send(record).get();
        }

        // Then - 모든 메시지 수신 확인
        List<ConsumerRecord<String, String>> allReceivedRecords = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        long timeout = 10000; // 10초 타임아웃

        // 모든 메시지를 수집
        while (allReceivedRecords.size() < messageCount &&
                (System.currentTimeMillis() - startTime) < timeout) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

            for (ConsumerRecord<String, String> record : records) {
                allReceivedRecords.add(record);
                log.info("Received - Key: {}, Value: {}, Partition: {}, Offset: {}",
                        record.key(), record.value(), record.partition(), record.offset());
            }
        }

        // 수신된 메시지 개수 확인
        assertThat(allReceivedRecords).hasSize(messageCount);

        // 각 메시지 내용 검증 (순서 보장 없이)
        Set<String> expectedKeys = new HashSet<>();
        Set<String> expectedValues = new HashSet<>();
        for (int i = 0; i < messageCount; i++) {
            expectedKeys.add("key-" + i);
            expectedValues.add("message-" + i);
        }

        Set<String> actualKeys = allReceivedRecords.stream()
                .map(ConsumerRecord::key)
                .collect(Collectors.toSet());
        Set<String> actualValues = allReceivedRecords.stream()
                .map(ConsumerRecord::value)
                .collect(Collectors.toSet());

        assertThat(actualKeys).isEqualTo(expectedKeys);
        assertThat(actualValues).isEqualTo(expectedValues);

        // 각 메시지 개별 확인 (키-값 매칭)
        Map<String, String> keyValueMap = allReceivedRecords.stream()
                .collect(Collectors.toMap(
                        ConsumerRecord::key,
                        ConsumerRecord::value
                ));

        for (int i = 0; i < messageCount; i++) {
            String expectedKey = "key-" + i;
            String expectedValue = "message-" + i;
            assertThat(keyValueMap).containsEntry(expectedKey, expectedValue);
        }

        log.info("모든 {} 개의 메시지가 성공적으로 수신되었습니다.", messageCount);

    }


    @Test
    void testAdminClient() {
        // Given & When
        var adminClient = kafkaConfig.getAdminClient();

        // Then
        assertNotNull(adminClient);

        // Clean up
        adminClient.close();
    }
}