package kr.hhplus.be.server.common.event.coupon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.kafka.TopicType;
import kr.hhplus.be.server.domain.coupon.service.CouponDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponUsageConsumer {
    
    private final ObjectMapper objectMapper;
    private final CouponDomainService couponDomainService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @KafkaListener(topics = "coupon-usage-request", groupId = "coupon-service-group")
    @Transactional
    public void handleCouponUsageRequest(String message) {
        try {
            log.info("쿠폰 사용 요청 수신 - message: {}", message);
            
            CouponUsageRequested event = objectMapper.readValue(message, CouponUsageRequested.class);
            
            // 쿠폰 사용 처리
            try {
                boolean success = couponDomainService.useCoupon(event.getUserCouponId(), event.getOrderId());
                
                if (success) {
                    CouponUsageCompleted completedEvent = CouponUsageCompleted.of(
                        event.getOrderId(), 
                        event.getUserId(), 
                        event.getUserCouponId()
                    );
                    publishEvent(TopicType.COUPON_USAGE_RESULT.getTopic(), completedEvent);
                    
                    log.info("쿠폰 사용 성공 - orderId: {}, userId: {}, userCouponId: {}", 
                            event.getOrderId(), event.getUserId(), event.getUserCouponId());
                } else {
                    CouponUsageFailed failedEvent = CouponUsageFailed.of(
                        event.getOrderId(), 
                        event.getUserId(), 
                        event.getUserCouponId(),
                        "쿠폰 사용 불가 (이미 사용됨 또는 만료됨)"
                    );
                    publishEvent(TopicType.COUPON_USAGE_RESULT.getTopic(), failedEvent);
                    
                    log.info("쿠폰 사용 실패 - orderId: {}, userId: {}, userCouponId: {}", 
                            event.getOrderId(), event.getUserId(), event.getUserCouponId());
                }
                
            } catch (Exception e) {
                CouponUsageFailed failedEvent = CouponUsageFailed.of(
                    event.getOrderId(), 
                    event.getUserId(), 
                    event.getUserCouponId(),
                    "쿠폰 사용 중 오류 발생: " + e.getMessage()
                );
                publishEvent(TopicType.COUPON_USAGE_RESULT.getTopic(), failedEvent);
                
                log.error("쿠폰 사용 중 오류 발생 - orderId: {}, userId: {}, userCouponId: {}", 
                        event.getOrderId(), event.getUserId(), event.getUserCouponId(), e);
            }
            
        } catch (JsonProcessingException e) {
            log.error("쿠폰 사용 요청 JSON 파싱 오류 - message: {}", message, e);
        } catch (Exception e) {
            log.error("쿠폰 사용 요청 처리 중 예상치 못한 오류 - message: {}", message, e);
        }
    }
    
    private void publishEvent(String topic, Object event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, eventJson);
        } catch (JsonProcessingException e) {
            log.error("이벤트 JSON 직렬화 오류 - topic: {}, event: {}", topic, event, e);
        } catch (Exception e) {
            log.error("이벤트 발행 오류 - topic: {}, event: {}", topic, event, e);
        }
    }
}
