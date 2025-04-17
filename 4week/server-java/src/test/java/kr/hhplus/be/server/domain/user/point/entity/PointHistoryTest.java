package kr.hhplus.be.server.domain.user.point.entity;

import kr.hhplus.be.server.infrastructure.point.entity.PointHistory;
import kr.hhplus.be.server.infrastructure.point.entity.PointTransactionType;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PointHistoryTest {

    @Test
    @DisplayName("충전 이력 생성 테스트")
    void 충전_이력_생성(){
        // give
        User user = User.builder()
                .id(1L)
                .userId("test")
                .name("사용자1")
                .build();
        BigDecimal amount = new BigDecimal(1_000_000L);

        // when
        PointHistory history = PointHistory.create(user, PointTransactionType.CHARGE, amount);

        // then
        assertThat(history.getUser()).isEqualTo(user);
        assertThat(history.getAmount()).isEqualTo(amount);
        assertThat(history.getType()).isEqualTo(PointTransactionType.CHARGE);
    }

    @Test
    @DisplayName("사용 이력 생성 테스트")
    void 사용_이력_생성(){
        // give
        User user = User.builder()
                .id(1L)
                .userId("test")
                .name("사용자1")
                .build();
        BigDecimal amount = new BigDecimal(1_000_000L);

        // when
        PointHistory history = PointHistory.create(user, PointTransactionType.USE, amount);

        //then
        assertThat(history.getUser()).isEqualTo(user);
        assertThat(history.getAmount()).isEqualTo(amount);
        assertThat(history.getType()).isEqualTo(PointTransactionType.USE);

    }

}