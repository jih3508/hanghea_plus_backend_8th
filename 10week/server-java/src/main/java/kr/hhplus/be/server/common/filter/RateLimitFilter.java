package kr.hhplus.be.server.common.filter;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * API 요청에 대한 속도 제한을 적용하는 필터
 * IP 주소 기반으로 분당 최대 요청 수를 제한함
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 60; // 분당 최대 요청 수

    // IP 별 요청 횟수를 추적하는 캐시
    private final LoadingCache<String, AtomicInteger> requestCountsPerIpAddress;

    public RateLimitFilter() {
        requestCountsPerIpAddress = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES) // 1분 후 만료
                .build(key -> new AtomicInteger(0));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 클라이언트 IP 주소 가져오기
        String clientIpAddress = getClientIP(request);

        // 속도 제한 초과 여부 확인
        if (isRateLimitExceeded(clientIpAddress)) {
            log.warn("IP {} 에 대한 요청 속도 제한 초과", clientIpAddress);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.");
            //return;
        }

        // 요청 처리
        filterChain.doFilter(request, response);
    }

    /**
     * 요청 속도 제한 초과 여부 확인
     */
    private boolean isRateLimitExceeded(String clientIpAddress) {
        AtomicInteger counter = requestCountsPerIpAddress.get(clientIpAddress);
        return counter.incrementAndGet() > MAX_REQUESTS_PER_MINUTE;
    }

    /**
     * 클라이언트 IP 주소 가져오기
     * 프록시나 로드 밸런서 뒤에 있을 경우를 고려함
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !xForwardedFor.equalsIgnoreCase("unknown")) {
            int idx = xForwardedFor.indexOf(',');
            if (idx > -1) {
                return xForwardedFor.substring(0, idx);
            } else {
                return xForwardedFor;
            }
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !xRealIp.equalsIgnoreCase("unknown")) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}