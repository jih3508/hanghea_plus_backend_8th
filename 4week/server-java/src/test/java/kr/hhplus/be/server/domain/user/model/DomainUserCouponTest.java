package kr.hhplus.be.server.domain.user.model;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.infrastructure.coupon.entity.CouponType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
class DomainUserCouponTest {

    @Test
    @DisplayName("발급시 이미 사용한 쿠폰이 있을 경우")
    void 발급_사용O_쿠폰(){

        DomainUserCoupon coupon = DomainUserCoupon.builder()
                .id(1L)
                .userId(1L)
                .couponId(1L)
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 3, 30, 12, 12))
                .isUsed(false)
                .build();

        assertThatThrownBy(()-> coupon.usedCoupon())
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("이미 사용한 쿠폰 입니다.");
    }

    @Test
    @DisplayName("쿠폰 사용 처리")
    void 발급_사용X_쿠폰(){

        DomainUserCoupon coupon = DomainUserCoupon.builder()
                .id(1L)
                .userId(1L)
                .couponId(1L)
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 3, 30, 12, 12))
                .isUsed(true)
                .build();

        coupon.usedCoupon();
        assertThat(coupon.getIsUsed()).isFalse();
    }

    @Test
    @DisplayName("쿠폰 할인 정액일때 계산")
    void 계산_정액(){
        // given
        DomainUserCoupon coupon = DomainUserCoupon.builder()
                .id(1L)
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 8, 30, 12, 12))
                .build();

        BigDecimal price = new BigDecimal(15_000);

        // when
        BigDecimal discount = coupon.getDiscountPrice(price);

        assertThat(discount).isEqualTo(new BigDecimal(10_000));
    }

    @Test
    @DisplayName("쿠폰 정률 계산 처리")
    void 정률_계산(){
        // given
        DomainUserCoupon coupon = DomainUserCoupon.builder()
                .id(1L)
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.RATE)
                .rate(10)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 8, 30, 12, 12))
                .build();

        BigDecimal price = new BigDecimal(10_000);

        // when
        BigDecimal discount = coupon.getDiscountPrice(price);

        assertThat(discount).isEqualTo(new BigDecimal(9_000));

    }

}