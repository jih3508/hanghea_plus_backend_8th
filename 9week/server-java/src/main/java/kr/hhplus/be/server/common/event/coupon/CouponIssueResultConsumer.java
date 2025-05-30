package kr.hhplus.be.server.common.event.coupon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.infrastructure.websocket.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueResultConsumer {
    
    private final ObjectMapper objectMapper;
    private final WebSocketNotificationService webSocketNotificationService;
    
    @KafkaListener(topics = "coupon-issue-result", groupId = "coupon-notification-group")
    public void handleCouponIssueResult(String message) {
        try {
            log.info("쿠폰 발급 결과 메시지 수신 - message: {}", message);
            
            // JSON에서 이벤트 타입 판별
            JsonNode jsonNode = objectMapper.readTree(message);
            
            // userCouponId 필드 존재 여부로 성공/실패 판별
            if (jsonNode.has("userCouponId")) {
                handleCouponIssueSuccess(message);
            } else if (jsonNode.has("reason")) {
                handleCouponIssueFailed(message);
            } else {
                log.warn("알 수 없는 쿠폰 발급 결과 이벤트 - message: {}", message);
            }
            
        } catch (Exception e) {
            log.error("쿠폰 발급 결과 처리 중 오류 발생 - message: {}", message, e);
        }
    }
    
    private void handleCouponIssueSuccess(String message) {
        try {
            CouponIssued event = objectMapper.readValue(message, CouponIssued.class);
            
            log.info("쿠폰 발급 성공 처리 - userId: {}, couponId: {}, requestId: {}, userCouponId: {}", 
                    event.getUserId(), event.getCouponId(), event.getRequestId(), event.getUserCouponId());
            
            // WebSocket으로 실시간 알림 전송
            webSocketNotificationService.notifyCouponIssueSuccess(
                event.getUserId(), 
                event.getRequestId(), 
                event.getUserCouponId()
            );
            
        } catch (JsonProcessingException e) {
            log.error("쿠폰 발급 성공 이벤트 JSON 파싱 오류 - message: {}", message, e);
        }
    }
    
    private void handleCouponIssueFailed(String message) {
        try {
            CouponIssueFailed event = objectMapper.readValue(message, CouponIssueFailed.class);
            
            log.info("쿠폰 발급 실패 처리 - userId: {}, couponId: {}, requestId: {}, reason: {}", 
                    event.getUserId(), event.getCouponId(), event.getRequestId(), event.getReason());
            
            // WebSocket으로 실시간 알림 전송
            webSocketNotificationService.notifyCouponIssueFailed(
                event.getUserId(), 
                event.getRequestId(), 
                event.getReason()
            );
            
        } catch (JsonProcessingException e) {
            log.error("쿠폰 발급 실패 이벤트 JSON 파싱 오류 - message: {}", message, e);
        }
    }
}
