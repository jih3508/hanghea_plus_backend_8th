package kr.hhplus.be.server.domain.user.point.service;

import kr.hhplus.be.server.domain.point.model.CreatePointHistory;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.service.PointHistoryService;
import kr.hhplus.be.server.infrastructure.point.entity.PointTransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PointHistoryServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PointHistoryServiceTest.class);

    @InjectMocks
    private PointHistoryService service;

    @Mock
    private PointHistoryRepository repository;


    @Test
    @DisplayName("충전 이력 저장 테스트")
    void 충전_이력(){
        // given
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(1_000_000);



        // when
        service.chargeHistory(userId, amount);
        CreatePointHistory result = CreatePointHistory.create(userId, amount, PointTransactionType.CHARGE);


        // then
        verify(repository, times(1)).create(result);
    }

    @Test
    @DisplayName("사용 이력 저장 테스트")
    void 사용_이력(){
        // given
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(1_000_000);


        // when
        service.useHistory(userId, amount);
        CreatePointHistory result = CreatePointHistory.create(userId, amount, PointTransactionType.USE);

        //then
        verify(repository, times(1)).create(result);

    }
}