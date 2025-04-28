package kr.hhplus.be.server.infrastructure.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * 시스템 상태를 모니터링하는 서비스
 * 데이터베이스 연결 상태, 메모리 사용량 등 시스템 상태 정보를 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemMonitoringService {

    private final DataSource dataSource;

    /**
     * 데이터베이스 연결 상태 확인
     * @return 데이터베이스 접근 가능 여부
     */
    public boolean isDatabaseHealthy() {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return result != null && result == 1;
        } catch (Exception e) {
            log.error("데이터베이스 상태 확인 실패", e);
            return false;
        }
    }

    /**
     * 메모리 사용량 정보 조회
     * @return 메모리 사용 정보가 담긴 Map
     */
    public Map<String, Object> getMemoryStats() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        Map<String, Object> memoryStats = new HashMap<>();

        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();

        memoryStats.put("heapUsedMB", heapUsed / (1024 * 1024));
        memoryStats.put("heapMaxMB", heapMax / (1024 * 1024));
        memoryStats.put("heapUsagePercent", (double) heapUsed / heapMax * 100);
        memoryStats.put("nonHeapUsedMB", nonHeapUsed / (1024 * 1024));

        return memoryStats;
    }

    /**
     * 전체 시스템 상태 정보 조회
     * @return 시스템 상태 정보
     */
    public Health getSystemHealth() {
        boolean dbHealthy = isDatabaseHealthy();
        Map<String, Object> memoryStats = getMemoryStats();

        double heapUsagePercent = (double) memoryStats.get("heapUsagePercent");

        if (!dbHealthy) {
            return Health.down()
                    .withDetail("database", "연결 실패")
                    .withDetails(memoryStats)
                    .build();
        }

        if (heapUsagePercent > 90) {
            return Health.outOfService()
                    .withDetail("memory", "높은 메모리 사용량")
                    .withDetails(memoryStats)
                    .build();
        }

        return Health.up()
                .withDetail("database", "연결됨")
                .withDetails(memoryStats)
                .build();
    }
}