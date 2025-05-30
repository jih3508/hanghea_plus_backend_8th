package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.domain.point.model.DomainPoint;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointDomainService {
    
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    
    /**
     * 포인트 차감 처리
     */
    @Transactional
    public boolean deductPoint(Long userId, BigDecimal amount) {
        try {
            DomainPoint point = pointRepository.findByUserId(userId);
            if (point == null) {
                log.warn("사용자 포인트 정보를 찾을 수 없음 - userId: {}", userId);
                return false;
            }
            
            if (point.getBalance().compareTo(amount) < 0) {
                log.warn("포인트 잔액 부족 - userId: {}, 현재잔액: {}, 요청금액: {}", 
                        userId, point.getBalance(), amount);
                return false;
            }
            
            // 포인트 차감
            point.deduct(amount);
            pointRepository.save(point);
            
            log.info("포인트 차감 완료 - userId: {}, 차감금액: {}, 남은잔액: {}", 
                    userId, amount, point.getBalance());
            return true;
            
        } catch (Exception e) {
            log.error("포인트 차감 중 오류 발생 - userId: {}, amount: {}", userId, amount, e);
            return false;
        }
    }
    
    /**
     * 주문 취소시 포인트 복원
     */
    @Transactional
    public boolean restorePointForOrder(Long orderId, Long userId) {
        try {
            // 주문에서 차감된 포인트 금액 조회
            // 실제로는 주문 정보에서 사용된 포인트 금액을 조회해야 함
            BigDecimal deductedAmount = getDeductedAmountFromOrder(orderId);
            
            if (deductedAmount == null || deductedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.info("주문에 사용된 포인트가 없음 - orderId: {}, userId: {}", orderId, userId);
                return true;
            }
            
            DomainPoint point = pointRepository.findByUserId(userId);
            if (point == null) {
                log.error("사용자 포인트 정보를 찾을 수 없음 - userId: {}", userId);
                return false;
            }
            
            // 포인트 복원
            point.restore(deductedAmount);
            pointRepository.save(point);
            
            log.info("포인트 복원 완료 - orderId: {}, userId: {}, 복원금액: {}, 현재잔액: {}", 
                    orderId, userId, deductedAmount, point.getBalance());
            return true;
            
        } catch (Exception e) {
            log.error("포인트 복원 중 오류 발생 - orderId: {}, userId: {}", orderId, userId, e);
            return false;
        }
    }
    
    /**
     * 주문에서 차감된 포인트 금액 조회
     * 실제 구현시에는 주문 엔티티에서 포인트 사용 금액을 조회
     */
    private BigDecimal getDeductedAmountFromOrder(Long orderId) {
        // TODO: 실제 주문 엔티티에서 포인트 사용 금액 조회 로직 구현
        // 임시로 포인트 히스토리에서 조회
        return pointHistoryRepository.findDeductedAmountByOrderId(orderId);
    }
}
