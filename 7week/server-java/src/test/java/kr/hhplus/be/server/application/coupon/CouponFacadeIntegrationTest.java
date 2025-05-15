package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.coupon.model.CreateCoupon;
import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.user.model.CreateUser;
import kr.hhplus.be.server.domain.user.model.CreateUserCoupon;
import kr.hhplus.be.server.domain.user.model.DomainUserCoupon;
import kr.hhplus.be.server.domain.user.model.UpdateUserCoupon;
import kr.hhplus.be.server.domain.user.repository.UserCouponRepository;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.infrastructure.coupon.entity.CouponType;
import kr.hhplus.be.server.support.IntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;


@DisplayName("쿠폰 파사드 통합 테스트")
class CouponFacadeIntegrationTest extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(CouponFacadeIntegrationTest.class);

    @Autowired
    private CouponFacade facade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        CreateUser createUser1 = CreateUser.builder()
                .name("테스트사용자1")
                .id("testuser1")
                .build();

        CreateUser createUser2 = CreateUser.builder()
                .name("테스트사용자2")
                .id("testuser2")
                .build();

        userRepository.create(createUser1);
        userRepository.create(createUser2);

        // 쿠폰 생성 - 수량 0개인 쿠폰
        CreateCoupon zeroCoupon = CreateCoupon.builder()
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(0)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 12, 30, 12, 12))
                .build();

        // 쿠폰 생성 - 정상 쿠폰
        CreateCoupon normalCoupon = CreateCoupon.builder()
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(100)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 12, 30, 12, 12))
                .build();

        // 쿠폰 생성 - 비율 할인 쿠폰
        CreateCoupon rateCoupon = CreateCoupon.builder()
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.RATE)
                .rate(10)
                .quantity(100)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 12, 30, 12, 12))
                .build();

        couponRepository.create(zeroCoupon);
        couponRepository.create(normalCoupon);
        couponRepository.create(rateCoupon);

        // 사용자1에게 쿠폰 미리 발급
        userCouponRepository.create(new CreateUserCoupon(1L, 2L));
    }

    @Test
    @DisplayName("존재하지 않는 사용자에게 쿠폰 발급 시 예외 발생")
    void 발급_없는_회원_통합테스트() {
        // given
        CouponIssueCommand command = CouponIssueCommand.builder()
                .userId(999L) // 존재하지 않는 사용자 ID
                .couponId(2L)
                .build();

        // when & then
        assertThatThrownBy(() -> facade.issue(command))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("없는 사용자 입니다.");
    }

    @Test
    @DisplayName("수량이 0인 쿠폰 발급 시 예외 발생")
    void 쿠폰_개수_부족_통합테스트() {
        // given
        CouponIssueCommand command = CouponIssueCommand.builder()
                .userId(1L)
                .couponId(1L) // 수량이 0인 쿠폰
                .build();

        // when & then
        assertThatThrownBy(() -> facade.issue(command))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("발급할 쿠폰이 없습니다.");
    }

    @Test
    @DisplayName("쿠폰 정상 발급 통합 테스트")
    void 쿠폰_발급_통합테스트() {
        // given
        CouponIssueCommand command = CouponIssueCommand.builder()
                .userId(2L)
                .couponId(2L) // 정상 쿠폰
                .build();

        // 발급 전 쿠폰 수량 확인
        DomainCoupon beforeCoupon = couponRepository.findById(2L).orElseThrow();
        int beforeQuantity = beforeCoupon.getQuantity();

        // when
        facade.issue(command);

        // then
        // 1. 사용자에게 쿠폰이 발급되었는지 확인
        List<DomainUserCoupon> userCoupons = userCouponRepository.findAllByUserId(2L);
        assertThat(userCoupons).isNotEmpty();
        assertThat(userCoupons.stream().anyMatch(uc -> uc.getCouponId().equals(2L))).isTrue();

        // 2. 쿠폰 수량이 감소했는지 확인
        DomainCoupon afterCoupon = couponRepository.findById(2L).orElseThrow();
        assertThat(afterCoupon.getQuantity()).isEqualTo(beforeQuantity - 1);
    }

    @Test
    @DisplayName("사용자가 보유한 쿠폰 목록 조회")
    void 사용자_쿠폰_목록_조회_통합테스트() {
        // given
        Long userId = 1L;

        // 추가 쿠폰 발급 (다양한 타입 테스트를 위함)
        CouponIssueCommand command = CouponIssueCommand.builder()
                .userId(userId)
                .couponId(3L) // 비율 할인 쿠폰
                .build();

        facade.issue(command);


        // when
        List<CouponMeCommand> result = facade.getMeCoupons(userId);

        // then
        assertThat(result).hasSize(2);

        // 쿠폰 타입 확인
        assertThat(result.stream()
                .map(CouponMeCommand::getType)
                .collect(Collectors.toList()))
                .containsExactlyInAnyOrder(CouponType.FLAT, CouponType.RATE);

        // 사용 여부 확인
        assertThat(result.stream()
                .filter(c -> c.getType() == CouponType.RATE)
                .findFirst().orElseThrow()
                .getIsUsed()).isTrue();

        assertThat(result.stream()
                .filter(c -> c.getType() == CouponType.FLAT)
                .findFirst().orElseThrow()
                .getIsUsed()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 발급 시 예외 발생")
    void 없는_쿠폰_발급_테스트() {
        // given
        CouponIssueCommand command = CouponIssueCommand.builder()
                .userId(1L)
                .couponId(999L) // 존재하지 않는 쿠폰
                .build();

        // when & then
        assertThatThrownBy(() -> facade.issue(command))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("쿠폰이 없습니다.");
    }

    @Test
    @DisplayName("중복 쿠폰 발급 처리 테스트")
    void 중복_쿠폰_발급_테스트() {
        // given
        CouponIssueCommand command = CouponIssueCommand.builder()
                .userId(1L)
                .couponId(2L) // 이미 발급된 쿠폰
                .build();

        // when
        facade.issue(command);

        // 같은 쿠폰 재발급 시도
        // then - 비즈니스 요구사항에 따라 중복 발급을 허용할 수도 있고, 예외를 던질 수도 있음
        // 현재 코드가 중복 발급을 어떻게 처리하는지에 따라 이 테스트를 수정해야 함
        // 여기서는 중복 발급이 허용된다고 가정
        facade.issue(command);

        // 중복 발급 후 사용자 쿠폰 확인
        List<CouponMeCommand> userCoupons = facade.getMeCoupons(1L);
        long count = userCoupons.stream()
                .filter(c -> c.getCouponId().equals(2L))
                .count();

        assertThat(count).isEqualTo(2); // 같은 쿠폰이 2개 있어야 함
    }

    @Test
    @DisplayName("없는 사용자의 쿠폰 목록 조회")
    void 없는_사용자_쿠폰_목록_조회() {
        // given
        Long nonExistingUserId = 999L;

        // when
        List<CouponMeCommand> result = facade.getMeCoupons(nonExistingUserId);

        // then
        assertThat(result).isEmpty();
    }

}