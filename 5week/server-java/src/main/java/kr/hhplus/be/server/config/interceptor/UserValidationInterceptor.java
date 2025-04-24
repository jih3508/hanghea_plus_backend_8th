package kr.hhplus.be.server.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 사용자 관련 API 요청에서 사용자 존재 여부를 확인하는 인터셉터
 * URL 경로에서 사용자 ID를 추출하여 유효성을 검증함
 */
@Component
public class UserValidationInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(UserValidationInterceptor.class);

    private final UserService userService;

    // 사용자 ID가 포함된 URL 패턴
    private final Pattern userIdPattern = Pattern.compile("/api/(?:point|coupons|orders)/([0-9]+)");

    public UserValidationInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        Matcher matcher = userIdPattern.matcher(path);

        // 패턴에 맞지 않는 경로는 검증 없이 통과
        if (!matcher.find()) {
            return true;
        }

        try {
            // URL에서 사용자 ID 추출
            Long userId = Long.parseLong(matcher.group(1));

            // 사용자 존재 여부 확인
            userService.findById(userId);
            log.debug("ID가 {}인 사용자가 존재하며 유효합니다", userId);

            return true;
        } catch (NumberFormatException e) {
            log.error("요청 경로에 유효하지 않은 사용자 ID: {}", path);
            throw new ApiExceptionResponse(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 ID 형식");
        } catch (ApiExceptionResponse e) {
            log.error("사용자 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("사용자 검증 중 예상치 못한 오류", e);
            throw new ApiExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 검증 중 오류 발생");
        }
    }
}