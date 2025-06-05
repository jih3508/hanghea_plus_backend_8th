package kr.hhplus.be.server.common.event.coupon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.event.coupon.model.CouponIssueFailed;
import kr.hhplus.be.server.common.event.coupon.model.CouponIssueRequested;
import kr.hhplus.be.server.common.event.coupon.model.CouponIssued;
import kr.hhplus.be.server.common.kafka.TopicType;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.service.UserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueStreamProcessor {
    
    private final ObjectMapper objectMapper;
    private final CouponService couponService;
    private final UserCouponService userCouponService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder) {
        KStream<String, String> stream = streamsBuilder.stream(TopicType.COUPON_ISSUE_REQUEST.getTopic());
        
        stream.foreach((key, value) -> {
            try {
                processCouponIssueRequest(value);
            } catch (Exception e) {
                log.error("쿠폰 발급 요청 처리 중 오류 발생 - key: {}, value: {}", key, value, e);
            }
        });
    }
    
    private void processCouponIssueRequest(String eventJson) {
        try {
            CouponIssueRequested event = objectMapper.readValue(eventJson, CouponIssueRequested.class);
            
            log.info("쿠폰 발급 요청 처리 시작 - userId: {}, couponId: {}, requestId: {}", 
                    event.getUserId(), event.getCouponId(), event.getRequestId());
            
            // 쿠폰 정보 조회 (최대 발급 수량 등)
            var coupon = couponService.getCoupon(event.getCouponId());
            
            // Redis에서 발급 가능 여부 확인 및 원자적 증가
            boolean issueSuccess = couponService.tryIssueCoupon(
                event.getCouponId(), 
                event.getUserId(), 
                coupon.getMaxCount()
            );
            
            if (issueSuccess) {
                // 발급 성공 - DB에 비동기 저장을 위한 이벤트 발행
                var userCoupon = userCouponService.getUseCoupon(event.getUserId(), event.getCouponId());
                
                CouponIssued issuedEvent = CouponIssued.of(
                    event.getUserId(), 
                    event.getCouponId(), 
                    event.getRequestId(),
                    userCoupon.getId()
                );
                
                publishEvent(TopicType.COUPON_ISSUE_RESULT.getTopic(), issuedEvent);
                
                log.info("쿠폰 발급 성공 이벤트 발행 - userId: {}, couponId: {}, requestId: {}, userCouponId: {}", 
                        event.getUserId(), event.getCouponId(), event.getRequestId(), userCoupon.getId());
            } else {
                // 발급 실패 - 실패 이벤트 발행
                CouponIssueFailed failedEvent = CouponIssueFailed.of(
                    event.getUserId(), 
                    event.getCouponId(), 
                    event.getRequestId(),
                    "쿠폰 발급 수량 초과 또는 이미 발급받음"
                );
                
                publishEvent(TopicType.COUPON_ISSUE_RESULT.getTopic(), failedEvent);
                
                log.info("쿠폰 발급 실패 이벤트 발행 - userId: {}, couponId: {}, requestId: {}", 
                        event.getUserId(), event.getCouponId(), event.getRequestId());
            }
            
        } catch (JsonProcessingException e) {
            log.error("쿠폰 발급 요청 JSON 파싱 오류 - eventJson: {}", eventJson, e);
        } catch (Exception e) {
            log.error("쿠폰 발급 요청 처리 중 예상치 못한 오류 - eventJson: {}", eventJson, e);
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
