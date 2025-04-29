package kr.hhplus.be.server.domain.coupon.entity;

import kr.hhplus.be.server.infrastructure.coupon.entity.Coupon;
import kr.hhplus.be.server.infrastructure.coupon.entity.CouponType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class CouponTest {

    @Test
    @DisplayName("쿠폰 만료기간이 지난는지 테스트")
    void 쿠폰_만료_기간_지남(){
        Coupon coupon = Coupon.builder()
                .id(1L)
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(100)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 3, 30, 12, 12))
                .build();

        assertThat(coupon.isExpired()).isTrue();

    }

    @Test
    @DisplayName("쿠폰 만료기간이 안 지남")
    void 만료_기간_유효(){
        Coupon coupon = Coupon.builder()
                .id(1L)
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(100)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 8, 30, 12, 12))
                .build();

        assertThat(coupon.isExpired()).isTrue();
    }

    @Test
    @DisplayName("쿠폰 개수가 부족해서 발급 못 할때")
    void 쿠폰_부족(){
        Coupon coupon = Coupon.builder()
                .id(1L)
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(0)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 8, 30, 12, 12))
                .build();

        assertThat(coupon.isExpired()).isFalse();
    }

    @Test
    @DisplayName("쿠폰 개수가 충분히 있을때")
    void 쿠폰_충분(){
        Coupon coupon = Coupon.builder()
                .id(1L)
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(1)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 8, 30, 12, 12))
                .build();


        assertThat(coupon.isExpired()).isTrue();
    }

    @Test
    @DisplayName("쿠폰 할인 정액일때 계산")
    void 계산_정액(){
        // given
        Coupon coupon = Coupon.builder()
                .id(1L)
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(1)
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
        Coupon coupon = Coupon.builder()
                .id(1L)
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.RATE)
                .rate(10)
                .quantity(1)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 8, 30, 12, 12))
                .build();

        BigDecimal price = new BigDecimal(10_000);

        // when
        BigDecimal discount = coupon.getDiscountPrice(price);
        
        assertThat(discount).isEqualTo(new BigDecimal(9_000));

    }


}