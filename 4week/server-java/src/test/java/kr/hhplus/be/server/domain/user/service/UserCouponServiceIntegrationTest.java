package kr.hhplus.be.server.domain.user.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.coupon.model.CreateCoupon;
import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.user.model.CreateUser;
import kr.hhplus.be.server.domain.user.model.CreateUserCoupon;
import kr.hhplus.be.server.domain.user.model.DomainUserCoupon;
import kr.hhplus.be.server.domain.user.repository.UserCouponRepository;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.infrastructure.coupon.entity.CouponType;
import kr.hhplus.be.server.infrastructure.user.entity.User;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("사용자 포인트 서비스 통합 테스트")
class UserCouponServiceIntegrationTest extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(UserCouponServiceIntegrationTest.class);

    @Autowired
    private UserCouponService service;

    @Autowired
    private UserCouponRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @BeforeEach
    public void setup() {
        CreateUser createUser = CreateUser.builder()
                .name("홍길동")
                .id("test")
                .build();

        User user = userRepository.create(createUser);

        CreateCoupon createCoupon = CreateCoupon.builder()
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(0)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 12, 30, 12, 12))
                .build();

        DomainCoupon coupon = couponRepository.create(createCoupon);

        repository.create(new CreateUserCoupon(user.getId(), coupon.getId()));

        createCoupon = CreateCoupon.builder()
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(100)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 12, 30, 12, 12))
                .build();

        coupon = couponRepository.create(createCoupon);

        repository.create(new CreateUserCoupon(user.getId(), coupon.getId()));

        createUser = CreateUser.builder()
                .name("강감찬")
                .id("test2")
                .build();

        user = userRepository.create(createUser);


        createCoupon = CreateCoupon.builder()
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(100)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 12, 30, 12, 12))
                .build();

        couponRepository.create(createCoupon);

    }

    @Test
    @DisplayName("사용가능 한 쿠폰이 없을때")
    void 사용X_쿠폰(){
        //given
        Long userId = 100L;
        Long couponId = 100L;

        //when & then
        assertThatThrownBy(()-> service.getUseCoupon(userId, couponId))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("사용가능 한 쿠폰 없습니다.");
    }

    @Test
    @DisplayName("사용가능 한 쿠폰이 있을때")
    void 사용O_쿠폰(){
        // given
        Long userId = 1L;
        Long couponId = 1L;

        //when
        DomainUserCoupon result = service.getUseCoupon(userId, couponId);

        assertThat(result).isNotNull();
    }


    @Test
    @DisplayName("발급 처리")
    void 발급_처리(){
        // given
        Long userId = 2L;

        CreateCoupon createCoupon = CreateCoupon.builder()
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(100)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 12, 30, 12, 12))
                .build();
        DomainCoupon coupon = couponRepository.create(createCoupon);

        //when
        service.issue(userId, coupon.getId());
        DomainUserCoupon result = service.getUseCoupon(userId, coupon.getId());

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("사용 처리")
    void 쿠폰_사용_처리(){
        // given
        Long userId = 2L;

        CreateCoupon createCoupon = CreateCoupon.builder()
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(100)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 12, 30, 12, 12))
                .build();
        DomainCoupon coupon = couponRepository.create(createCoupon);

        //when
        service.useCoupon(userId, coupon.getId());
        DomainUserCoupon result = service.getUseCoupon(userId, coupon.getId());

        //then
        assertThat(result).isNotNull();
        assertThat(result.getIsUsed()).isFalse();
    }

    @Test
    @DisplayName("사용자 쿠폰 목록 조회")
    void 쿠폰_목록_조회(){
        //given
        Long userId = 1L;

        // when
        List<DomainUserCoupon> result = service.getUserCoupons(userId);

        //then
        assertThat(result).isNotEmpty();
    }


}