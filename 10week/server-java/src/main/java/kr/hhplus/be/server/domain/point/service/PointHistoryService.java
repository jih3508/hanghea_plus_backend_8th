package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.domain.point.model.CreatePointHistory;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import kr.hhplus.be.server.infrastructure.point.entity.PointHistory;
import kr.hhplus.be.server.infrastructure.point.entity.PointTransactionType;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;


    /*
     * method: chargeHistory
     * description: 충전 이력 생성
     */
    public void chargeHistory(Long userId, BigDecimal amount) {
        CreatePointHistory history = CreatePointHistory.create(userId, amount, PointTransactionType.CHARGE);
        pointHistoryRepository.create(history);
    }

    /*
     method: chargeHistory
     * description: 사용 이력 생성
     */
    public void useHistory(Long userId, BigDecimal amount) {
        CreatePointHistory history = CreatePointHistory.create(userId, amount, PointTransactionType.USE);
        pointHistoryRepository.create(history);
    }
}
