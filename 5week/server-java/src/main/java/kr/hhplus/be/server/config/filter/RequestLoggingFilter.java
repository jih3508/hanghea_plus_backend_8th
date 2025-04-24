package kr.hhplus.be.server.config.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

/**
 * 모든 HTTP 요청과 응답을 로깅하는 필터
 * 각 요청에 고유 ID를 부여하여 추적이 가능하도록 함
 */
@Component
public class RequestLoggingFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 요청 추적을 위한 고유 ID 생성
        String requestId = UUID.randomUUID().toString().replace("-", "");
        MDC.put("requestId", requestId);

        // 요청과 응답을 여러 번 읽을 수 있도록 래핑
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

        // 요청 처리 시작 시간 기록
        long startTime = System.currentTimeMillis();

        try {
            // 요청 정보 로깅
            logRequest(wrappedRequest);

            // 다음 필터 또는 컨트롤러로 요청 전달
            chain.doFilter(wrappedRequest, wrappedResponse);

            // 응답 정보 로깅
            long duration = System.currentTimeMillis() - startTime;
            logResponse(wrappedResponse, duration);
        } finally {
            // 응답 내용을 원래 응답 객체에 복사
            wrappedResponse.copyBodyToResponse();
            MDC.remove("requestId");
        }
    }

    /**
     * HTTP 요청 정보를 로깅
     */
    private void logRequest(ContentCachingRequestWrapper request) {
        log.info("요청: {} {} (Content-Type: {})",
                request.getMethod(),
                request.getRequestURI(),
                request.getContentType());
    }

    /**
     * HTTP 응답 정보를 로깅
     */
    private void logResponse(ContentCachingResponseWrapper response, long duration) {
        log.info("응답: {} ({}ms, Content-Type: {})",
                response.getStatus(),
                duration,
                response.getContentType());
    }
}