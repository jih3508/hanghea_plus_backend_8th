package kr.hhplus.be.server.application.coupon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.event.coupon.model.CouponIssueRequested;
import kr.hhplus.be.server.common.kafka.TopicType;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.model.DomainUser;
import kr.hhplus.be.server.domain.user.service.UserCouponService;
import kr.hhplus.be.server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponFacade {

    private final CouponService service;
    private final UserService userService;
    private final UserCouponService userCouponService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /*
     * method: issue
     * description: 쿠폰발급 (기존 동기 방식)
     */
    //@DistributedLock(key = "#command.getCouponId()", type = LockType.COUPON , strategy = LockStrategy.PUB_SUB_LOCK)
    @Transactional
    public void issue(CouponIssueCommand command) {

        // 쿠폰 개수 있지는지 먼저 조회
        service.checkCouponCounter(command.getCouponId());

        try {

            DomainUser user = userService.findById(command.getUserId());
            userCouponService.issue(user.getId(), command.getCouponId());

        }catch (RuntimeException e) {

            // 중간에 실패 하면 다시 원복 시킴
            service.resetCouponCounter(command.getCouponId());
        }

    }

    /*
     * method: issueAsync
     * description: 쿠폰발급 (비동기 방식)
     */
    public String issueAsync(CouponIssueCommand command) {
        try {
            // 요청 ID 생성
            String requestId = UUID.randomUUID().toString();
            
            log.info("비동기 쿠폰 발급 요청 시작 - userId: {}, couponId: {}, requestId: {}", 
                    command.getUserId(), command.getCouponId(), requestId);
            
            // 사용자 존재 여부 확인
            userService.findById(command.getUserId());
            
            // 쿠폰 발급 요청 이벤트 생성 및 발행
            CouponIssueRequested event = CouponIssueRequested.of(
                command.getUserId(), 
                command.getCouponId(), 
                requestId
            );
            
            publishEvent(TopicType.COUPON_ISSUE_REQUEST.getTopic(), event);
            
            log.info("비동기 쿠폰 발급 요청 완료 - userId: {}, couponId: {}, requestId: {}", 
                    command.getUserId(), command.getCouponId(), requestId);
            
            return requestId;
            
        } catch (Exception e) {
            log.error("비동기 쿠폰 발급 요청 중 오류 발생 - userId: {}, couponId: {}", 
                    command.getUserId(), command.getCouponId(), e);
            throw e;
        }
    }

    /*
     * method: getMeCoupons
     * description: 쿠폰 내것 조회
     */
    @Transactional(readOnly = true)
    public List<CouponMeCommand>  getMeCoupons(Long userId) {
        return userCouponService.getUserCoupons(userId).stream()
                .map(CouponMeCommand::toCommand).toList();
    }
    
    private void publishEvent(String topic, Object event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, eventJson);
        } catch (JsonProcessingException e) {
            log.error("이벤트 JSON 직렬화 오류 - topic: {}, event: {}", topic, event, e);
            throw new RuntimeException("이벤트 발행 실패", e);
        }
    }
}
