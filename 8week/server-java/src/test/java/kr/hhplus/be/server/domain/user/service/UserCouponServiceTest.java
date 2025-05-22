package kr.hhplus.be.server.domain.user.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.user.model.DomainUserCoupon;
import kr.hhplus.be.server.infrastructure.coupon.entity.Coupon;
import kr.hhplus.be.server.infrastructure.coupon.entity.CouponType;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import kr.hhplus.be.server.domain.user.repository.UserCouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCouponServiceTest {

    private static final Logger log = LoggerFactory.getLogger(UserCouponServiceTest.class);

    @InjectMocks
    private UserCouponService service;

    @Mock
    private UserCouponRepository repository;


    @Test
    @DisplayName("사용가능 한 쿠폰이 없을때")
    void 사용X_쿠폰(){

        //given, when
        when(repository.findByUserIdAndCouponIdLock(anyLong(), anyLong())).thenThrow(new ApiExceptionResponse(HttpStatus.NOT_FOUND, "사용가능 한 쿠폰 없습니다."));

        // then
        assertThatThrownBy(() -> service.getUseCoupon(anyLong(), anyLong()))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("사용가능 한 쿠폰 없습니다.");
    }

    @Test
    @DisplayName("사용 가능 한 쿠폰이 있을때")
    void 사용O_쿠폰(){
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


        DomainUserCoupon expected = DomainUserCoupon.builder()
                .id(1L)
                .userId(1L)
                .couponId(coupon.getId())
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 3, 30, 12, 12))
                .isUsed(true)
                .build();

        when(repository.findByUserIdAndCouponIdLock(1L, 1L)).thenReturn(Optional.of(expected));

        DomainUserCoupon result = service.getUseCoupon(1L, 1L);

        assertThat(result).isEqualTo(coupon);
    }
}