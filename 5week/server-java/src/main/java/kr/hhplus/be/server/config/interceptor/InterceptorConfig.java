package kr.hhplus.be.server.config.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 애플리케이션에서 사용할 인터셉터를 등록하는 설정 클래스
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final UserValidationInterceptor userValidationInterceptor;

    public InterceptorConfig(UserValidationInterceptor userValidationInterceptor) {
        this.userValidationInterceptor = userValidationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 사용자 검증 인터셉터 등록 및 적용 경로 설정
        registry.addInterceptor(userValidationInterceptor)
                .addPathPatterns(
                        "/api/point/{userId}/**", // 포인트 관련 API
                        "/api/coupons/{userId}/**", // 쿠폰 관련 API
                        "/api/orders/{userId}/**"  // 주문 관련 API
                );
    }
}