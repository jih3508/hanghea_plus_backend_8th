package kr.hhplus.be.server.support;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@Configuration
public class TestcontainersConfig implements BeforeAllCallback {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("hhplus_test")
            .withUsername("test")
            .withPassword("test");


    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        System.out.println(mysql.getJdbcUrl());
        registry.add("spring.datasource.url", ()-> mysql.getJdbcUrl() + "?characterEncoding=UTF-8&serverTimezone=UTC");
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }


    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (mysql.isRunning()) {
            mysql.stop();
        }

        mysql.start();
    }
}
