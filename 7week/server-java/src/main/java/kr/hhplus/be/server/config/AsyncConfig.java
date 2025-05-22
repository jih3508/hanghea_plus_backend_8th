package kr.hhplus.be.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // 필요에 따라 AsyncConfigurer 인터페이스를 구현하여
    // ThreadPoolTaskExecutor를 커스터마이징할 수 있습니다.
}
