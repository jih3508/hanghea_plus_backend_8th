package kr.hhplus.be.server.domain.point.model;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DomainPointTest {

    final BigDecimal MAX_POINT = new BigDecimal(1_000_000_000L);

    private static final Logger log = LoggerFactory.getLogger(DomainPointTest.class);


    @Test
    @DisplayName("잔액 충전시 한도 초과로 실패")
    void 충전후_한도_초과(){
        // given
        DomainPoint point = DomainPoint.builder()
                .id(1L)
                .userId(1L)
                .point(MAX_POINT)
                .build();

        BigDecimal chargePoint = new BigDecimal(1_000_000);

        assertThatThrownBy(() -> point.charge(chargePoint))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("충전후 포인트가 한도 초과 되었습니다.");

    }

    @Test
    @DisplayName("잔액 충전 포인트가 0원 이하일때 실패")
    void 충전_금액_0이하(){
        // given
        DomainPoint point = DomainPoint.builder()
                .id(1L)
                .userId(1L)
                .point(MAX_POINT)
                .build();

        BigDecimal chargePoint = new BigDecimal(-100000);

        assertThatThrownBy(() -> point.charge(chargePoint))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("충전 포인트 금액이 1원 이상이여야 합니다.");

    }

    @Test
    @DisplayName("잔액 정상적인 충전 테스트")
    void 충전(){
        // given
        DomainPoint point = DomainPoint.builder()
                .id(1L)
                .userId(1L)
                .point(new BigDecimal(1_000_000))
                .build();

        BigDecimal chargePoint = new BigDecimal(1_000_000);

        // when
        point.charge(chargePoint);

        // then
        assertThat(point.getPoint()).isEqualTo(chargePoint.add(new BigDecimal(1_000_000)));
    }

    @Test
    @DisplayName("잔액 사용시 사용금액 보다 적어서 실패!!")
    void 잔액_사용_실패(){
        // given
        DomainPoint point = DomainPoint.builder()
                .id(1L)
                .userId(1L)
                .point(new BigDecimal(1_000_000))
                .build();

        BigDecimal usePoint = new BigDecimal(2_000_000);

        // when
        assertThatThrownBy(() -> point.use(usePoint))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("잔액 부족이 부족합니다. 충전후 결제 요청 드립니다.");

    }

    @Test
    @DisplayName("잔액 정상적인 사용 테스트")
    void 잔액_사용(){
        // given
        DomainPoint point = DomainPoint.builder()
                .id(1L)
                .userId(1L)
                .point(new BigDecimal(1_000_000))
                .build();

        BigDecimal usePoint = new BigDecimal(500_000);

        point.use(usePoint);

        log.info(new BigDecimal(1_000_000).subtract(usePoint).toString());
        assertThat(point.getPoint()).isEqualTo(new BigDecimal(1_000_000).subtract(usePoint));
    }


}