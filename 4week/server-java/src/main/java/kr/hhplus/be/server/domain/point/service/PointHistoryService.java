package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.point.entity.PointHistory;
import kr.hhplus.be.server.domain.point.entity.PointTransactionType;
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
     * description: 충전 이력
     */
    public PointHistory chargeHistory(User user, BigDecimal amount) {
        PointHistory history = PointHistory.create(user, PointTransactionType.CHARGE,  amount);
        return pointHistoryRepository.save(history);
    }

    /*
     method: chargeHistory
     * description: 사용 이력
     */
    public PointHistory useHistory(User user, BigDecimal amount) {
        PointHistory history = PointHistory.create(user, PointTransactionType.USE,  amount);
        return pointHistoryRepository.save(history);
    }
}
