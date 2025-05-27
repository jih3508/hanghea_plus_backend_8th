package kr.hhplus.be.server.application.point;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.point.model.DomainPoint;
import kr.hhplus.be.server.domain.point.model.DomainPointHistory;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.user.model.CreateUser;
import kr.hhplus.be.server.domain.user.model.DomainUser;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("포인트 facade 통합 테스트")
class PointFacadeIntegrationTest extends IntegrationTest {

    final BigDecimal MAX_POINT = new BigDecimal(1_000_000_000L);

    @Autowired
    private PointFacade pointFacade;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    private DomainUser testUser;
    private DomainPoint initialPoint;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        CreateUser createUser = CreateUser.builder()
                .id("testUser")
                .name("테스트 사용자")
                .build();
        testUser = userRepository.create(createUser).toDomain();

        // 초기 포인트 설정
        initialPoint = pointRepository.create(testUser.getId());
    }

    @Test
    @DisplayName("사용자 포인트 조회 테스트")
    void getPointTest() {
        // when
        BigDecimal point = pointFacade.getPoint(testUser.getId());

        // then
        assertThat(point).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("없는 사용자의 포인트 조회시 예외 발생")
    void getPointWithNonExistentUserTest() {
        // when & then
        assertThatThrownBy(() -> pointFacade.getPoint(9999L))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessageContaining("없는 사용자");
    }

    @Test
    @DisplayName("정상적인 포인트 충전 테스트")
    void chargePointTest() {
        // given
        BigDecimal chargeAmount = new BigDecimal(100_000);
        PointChargeCommand command = new PointChargeCommand(testUser.getId(), chargeAmount);

        // when
        BigDecimal updatedPoint = pointFacade.charge(command);

        // then
        assertThat(updatedPoint).isEqualTo(chargeAmount);

        // DB 검증
        Optional<DomainPoint> pointInDB = pointRepository.findByUserIdLock(testUser.getId());
        assertThat(pointInDB).isPresent();
        assertThat(pointInDB.get().getPoint()).isEqualTo(chargeAmount);

        // 포인트 히스토리 검증
        List<DomainPointHistory> histories = pointHistoryRepository.findByUserId(testUser.getId());
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getAmount()).isEqualTo(chargeAmount);
    }

    @Test
    @DisplayName("없는 사용자 포인트 충전시 예외 발생")
    void chargePointWithNonExistentUserTest() {
        // given
        PointChargeCommand command = new PointChargeCommand(9999L, new BigDecimal(10_000));

        // when & then
        assertThatThrownBy(() -> pointFacade.charge(command))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessageContaining("없는 사용자 입니다.");
    }

    @Test
    @DisplayName("한도 초과 포인트 충전시 예외 발생")
    void chargePointExceedingLimitTest() {
        // given
        PointChargeCommand command = new PointChargeCommand(testUser.getId(), MAX_POINT.add(BigDecimal.ONE));

        // when & then
        assertThatThrownBy(() -> pointFacade.charge(command))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessageContaining("충전후 포인트가 한도 초과 되었습니다.");
    }

    @Test
    @DisplayName("여러 번 충전 후 포인트 누적 테스트")
    void multipleChargesTest() {
        // given
        BigDecimal firstCharge = new BigDecimal(10_000);
        BigDecimal secondCharge = new BigDecimal(20_000);

        // when
        pointFacade.charge(new PointChargeCommand(testUser.getId(), firstCharge));
        BigDecimal finalPoint = pointFacade.charge(new PointChargeCommand(testUser.getId(), secondCharge));

        // then
        BigDecimal expectedTotal = firstCharge.add(secondCharge);
        assertThat(finalPoint).isEqualTo(expectedTotal);

        // DB 검증
        Optional<DomainPoint> pointInDB = pointRepository.findByUserIdLock(testUser.getId());
        assertThat(pointInDB).isPresent();
        assertThat(pointInDB.get().getPoint()).isEqualTo(expectedTotal);

        // 포인트 히스토리 검증
        List<DomainPointHistory> histories = pointHistoryRepository.findByUserId(testUser.getId());
        assertThat(histories).hasSize(2);
        assertThat(histories.get(0).getAmount()).isEqualTo(firstCharge);
        assertThat(histories.get(1).getAmount()).isEqualTo(secondCharge);
    }
}