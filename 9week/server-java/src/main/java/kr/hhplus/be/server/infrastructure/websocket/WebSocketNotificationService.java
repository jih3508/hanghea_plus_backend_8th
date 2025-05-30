package kr.hhplus.be.server.infrastructure.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * 특정 사용자에게 쿠폰 발급 성공 알림 전송
     */
    public void notifyCouponIssueSuccess(Long userId, String requestId, Long userCouponId) {
        try {
            Map<String, Object> message = Map.of(
                "type", "COUPON_ISSUE_SUCCESS",
                "requestId", requestId,
                "userCouponId", userCouponId,
                "message", "쿠폰이 성공적으로 발급되었습니다."
            );
            
            messagingTemplate.convertAndSendToUser(
                userId.toString(), 
                "/queue/coupon", 
                message
            );
            
            log.info("쿠폰 발급 성공 알림 전송 완료 - userId: {}, requestId: {}, userCouponId: {}", 
                    userId, requestId, userCouponId);
        } catch (Exception e) {
            log.error("쿠폰 발급 성공 알림 전송 실패 - userId: {}, requestId: {}", userId, requestId, e);
        }
    }
    
    /**
     * 특정 사용자에게 쿠폰 발급 실패 알림 전송
     */
    public void notifyCouponIssueFailed(Long userId, String requestId, String reason) {
        try {
            Map<String, Object> message = Map.of(
                "type", "COUPON_ISSUE_FAILED",
                "requestId", requestId,
                "reason", reason,
                "message", "쿠폰 발급에 실패했습니다: " + reason
            );
            
            messagingTemplate.convertAndSendToUser(
                userId.toString(), 
                "/queue/coupon", 
                message
            );
            
            log.info("쿠폰 발급 실패 알림 전송 완료 - userId: {}, requestId: {}, reason: {}", 
                    userId, requestId, reason);
        } catch (Exception e) {
            log.error("쿠폰 발급 실패 알림 전송 실패 - userId: {}, requestId: {}", userId, requestId, e);
        }
    }
}
