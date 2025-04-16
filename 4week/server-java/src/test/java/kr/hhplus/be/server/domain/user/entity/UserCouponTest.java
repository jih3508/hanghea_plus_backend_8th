package kr.hhplus.be.server.domain.user.entity;

import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.entity.CouponType;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import kr.hhplus.be.server.infrastructure.user.entity.UserCoupon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserCouponTest {


    @Test
    @DisplayName("쿠폰 사용 처리")
    void 쿠폰_사용_처리(){
        // given

        User user = User.builder()
                .id(1L)
                .userId("test")
                .name("테스터")
                .build();


        Coupon coupon = Coupon.builder()
                .id(1L)
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(100)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 3, 30, 12, 12))
                .build();


        UserCoupon userCoupon = UserCoupon.builder()
                .user(user)
                .coupon(coupon)
                .isUsed(true)
                .build();

        userCoupon.usedCoupon();

        assertThat(userCoupon.getIsUsed()).isFalse();
    }
}