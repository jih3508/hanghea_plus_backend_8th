package kr.hhplus.be.server.config.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * API 요청에 대한 인증을 처리하는 필터
 * API 키를 통해 인증을 수행하며, 인증되지 않은 요청은 거부함
 */
@Component
@Order(1) // 다른 필터보다 먼저 실행
public class AuthenticationFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    // 인증이 필요 없는 경로 목록
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/products", // 상품 조회 API
            "/api/swagger-ui", // Swagger UI
            "/api/v3/api-docs", // OpenAPI 문서
            "/actuator" // 모니터링 엔드포인트
    );

    @Value("${api.key:valid-api-key-for-testing}")
    private String validApiKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // 공개 경로인지 확인하여 인증 없이 통과
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 헤더에서 API 키 추출
        String apiKey = httpRequest.getHeader("X-API-KEY");

        // API 키 검증
        if (apiKey == null || !isValidApiKey(apiKey)) {
            log.warn("인증되지 않은 접근 시도: {}", path);
            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpResponse.getWriter().write("{\"error\": \"인증되지 않은 접근입니다.\"}");
            return;
        }

        // 인증 성공 시 다음 필터로 전달
        chain.doFilter(request, response);
    }

    /**
     * 요청 경로가 공개 경로인지 확인
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * API 키 유효성 검증
     */
    private boolean isValidApiKey(String apiKey) {
        // 실제 운영 환경에서는 데이터베이스나 보안 저장소에서 검증해야 함
        return validApiKey.equals(apiKey);
    }
}