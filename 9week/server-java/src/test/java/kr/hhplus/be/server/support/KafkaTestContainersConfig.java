package kr.hhplus.be.server.support;

import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;

import java.time.Duration;
import java.util.Properties;
import java.util.List;

@Configuration
public class KafkaTestContainersConfig {

    private static final KafkaContainer kafkaContainer;

    static {
        kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
                .withReuse(true)  // 컨테이너 재사용으로 테스트 속도 향상
                .withStartupTimeout(Duration.ofMinutes(2));  // 시작 타임아웃 설정
        kafkaContainer.setPortBindings(List.of("9092:9092"));
        kafkaContainer.start();
    }

    public static String getBootstrapServers() {
        return kafkaContainer.getBootstrapServers();
    }

    public AdminClient getAdminClient() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        return AdminClient.create(props);
    }
}