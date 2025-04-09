package kr.hhplus.be.server.domain.user.point.service;

import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.user.point.entity.PointHistory;
import kr.hhplus.be.server.domain.user.point.entity.PointTransactionType;
import kr.hhplus.be.server.domain.user.point.repository.PointHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointHistoryServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PointHistoryServiceTest.class);

    @InjectMocks
    private PointHistoryService  service;

    @Mock
    private PointHistoryRepository repository;


    @Test
    @DisplayName("충전 이력 저장 테스트")
    void 충전_이력(){
        // given
        User user = User.builder()
                .id(1L)
                .userId("test")
                .name("테스터")
                .build();
        BigDecimal amount = BigDecimal.valueOf(1_000_000);

        PointHistory history = PointHistory.builder()
                .id(1L)
                .user(user)
                .amount(amount)
                .type(PointTransactionType.CHARGE)
                .build();

        // when
        when(repository.save(any())).thenReturn(history);
        PointHistory result = service.chargeHistory(user, amount);


        // then
        assertThat(result.getType()).isEqualTo(PointTransactionType.CHARGE);
        verify(repository, times(1)).save(any());
    }

    @Test
    @DisplayName("사용 이력 저장 테스트")
    void 사용_이력(){
        // given
        User user = User.builder()
                .id(1L)
                .userId("test")
                .name("테스터")
                .build();
        BigDecimal amount = BigDecimal.valueOf(1_000_000);

        PointHistory history = PointHistory.builder()
                .id(1L)
                .user(user)
                .amount(amount)
                .type(PointTransactionType.USE)
                .build();

        // when
        when(repository.save(any())).thenReturn(history);
        PointHistory result = service.useHistory(user, amount);

        //then
        assertThat(result.getType()).isEqualTo(PointTransactionType.USE);
        verify(repository, times(1)).save(any());

    }
}