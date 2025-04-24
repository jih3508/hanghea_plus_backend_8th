package kr.hhplus.be.server.config.filter;

import kr.hhplus.be.server.common.filter.AuthenticationFilter;
import kr.hhplus.be.server.common.filter.RateLimitFilter;
import kr.hhplus.be.server.common.filter.RequestLoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 애플리케이션에서 사용할 필터를 등록하는 설정 클래스
 */
@Configuration
public class FilterConfig {

    /**
     * 요청 로깅 필터 등록
     */
    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> loggingFilterRegistration() {
        FilterRegistrationBean<RequestLoggingFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(new RequestLoggingFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registrationBean;
    }

    /**
     * 인증 필터 등록
     */
    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authFilterRegistration() {
        FilterRegistrationBean<AuthenticationFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(new AuthenticationFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);

        return registrationBean;
    }

    /**
     * 요청 속도 제한 필터 등록
     */
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration() {
        FilterRegistrationBean<RateLimitFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(new RateLimitFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);

        return registrationBean;
    }
}