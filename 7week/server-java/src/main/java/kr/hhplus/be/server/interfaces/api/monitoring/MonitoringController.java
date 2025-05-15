package kr.hhplus.be.server.interfaces.api.monitoring;

import kr.hhplus.be.server.infrastructure.monitoring.SystemMonitoringService;
import kr.hhplus.be.server.interfaces.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 시스템 모니터링 정보를 제공하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitoringController {

    private final SystemMonitoringService monitoringService;

    /**
     * 시스템 건강 상태 조회 API
     * @return 시스템 건강 상태 정보
     */
    @GetMapping("/health")
    public ApiResponse<Health> getHealth() {
        long startTime = System.currentTimeMillis();
        Health health = monitoringService.getSystemHealth();
        long endTime = System.currentTimeMillis();

        log.info("상태 확인 API 호출 처리 시간: {}ms", endTime - startTime);

        return ApiResponse.ok(health);
    }

    /**
     * 메모리 사용량 조회 API
     * @return 메모리 사용량 정보
     */
    @GetMapping("/memory")
    public ApiResponse<Map<String, Object>> getMemoryStats() {
        long startTime = System.currentTimeMillis();
        Map<String, Object> memoryStats = monitoringService.getMemoryStats();
        long endTime = System.currentTimeMillis();

        log.info("메모리 상태 API 호출 처리 시간: {}ms", endTime - startTime);

        return ApiResponse.ok(memoryStats);
    }

    /**
     * 전체 시스템 통계 조회 API
     * @return 시스템 통계 정보
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        // 데이터베이스 상태 정보
        stats.put("database", Map.of(
                "isHealthy", monitoringService.isDatabaseHealthy()
        ));

        // 메모리 상태 정보
        stats.put("memory", monitoringService.getMemoryStats());

        // 런타임 정보
        Runtime runtime = Runtime.getRuntime();
        stats.put("runtime", Map.of(
                "availableProcessors", runtime.availableProcessors(),
                "freeMemory", runtime.freeMemory() / (1024 * 1024),
                "maxMemory", runtime.maxMemory() / (1024 * 1024),
                "totalMemory", runtime.totalMemory() / (1024 * 1024)
        ));

        // JVM 정보
        stats.put("jvm", Map.of(
                "javaVersion", System.getProperty("java.version"),
                "javaVendor", System.getProperty("java.vendor"),
                "javaHome", System.getProperty("java.home")
        ));

        return ApiResponse.ok(stats);
    }
}