package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.coupon.model.CreateCoupon;
import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.infrastructure.coupon.entity.Coupon;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("포인트 서비스 통합 테스트")
class CouponServiceIntegrationTest extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(CouponServiceIntegrationTest.class);

    @Autowired
    private CouponService service;

    @Autowired
    private CouponRepository repository;

    @BeforeEach
    void setUp() {
        CreateCoupon createCoupon = CreateCoupon.builder()
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(0)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 12, 30, 12, 12))
                .build();

        repository.create(createCoupon);


        createCoupon = CreateCoupon.builder()
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(100)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 12, 30, 12, 12))
                .build();

        repository.create(createCoupon);

    }

    @Test
    @DisplayName("조회시 쿠폰이 없는 경우")
    void 조회_쿠폰X(){
        // given
        Long couponId = 100L;

        // when, then
        assertThatThrownBy(() -> service.issueCoupon(couponId))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("쿠폰이 없습니다.");

    }

    @Test
    @DisplayName("조회시 쿠폰이 있을 경우")
    void 조회_쿠폰O(){
        // given
        Long couponId = 1L;

        //when
        DomainCoupon coupon = service.issueCoupon(couponId);

        //then
        assertThat(coupon).isNotNull();
    }

    @Test
    @DisplayName("발급시 발급 불가능 한 쿠폰을 발급 할때")
    void 발급_쿠폰X(){
        //given
        Long couponId = 1L;

        //when
        assertThatThrownBy(() -> service.issueCoupon(couponId))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("발급할 쿠폰이 없습니다.");


    }

    @Test
    @DisplayName("발급할 쿠폰이 있을 경우 쿠폰을 발급 한다.")
    void 발급_쿠폰O(){
        //given
        Long couponId = 2L;
        DomainCoupon coupon = service.getCoupon(couponId);

        //when
        DomainCoupon result = service.issueCoupon(couponId);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(coupon.getQuantity());

    }

}