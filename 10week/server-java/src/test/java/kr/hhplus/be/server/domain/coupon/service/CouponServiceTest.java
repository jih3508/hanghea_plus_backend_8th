package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.model.UpdateCoupon;
import kr.hhplus.be.server.infrastructure.coupon.entity.Coupon;
import kr.hhplus.be.server.infrastructure.coupon.entity.CouponType;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
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
class CouponServiceTest {

    private static final Logger log = LoggerFactory.getLogger(CouponServiceTest.class);

    @InjectMocks
    private CouponService service;


    @Mock
    private CouponRepository couponRepository;

    @Test
    @DisplayName("조회 할 쿠폰이 없는 경우")
    void 쿠폰(){

        //given, when
        when(couponRepository.findById(anyLong())).thenThrow( new ApiExceptionResponse(HttpStatus.NOT_FOUND, "쿠폰이 없습니다."));

        assertThatThrownBy(() -> service.issueCoupon(anyLong()))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("쿠폰이 없습니다.");
    }

    @Test
    @DisplayName("발급할 쿠폰 없을 경우")
    void 발급_쿠폰X(){
        DomainCoupon coupon = DomainCoupon.builder()
                .id(1L)
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(0)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 12, 30, 12, 12))
                .build();

        //given, when
        when(couponRepository.findById(anyLong())).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> service.issueCoupon(1L))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("발급할 쿠폰이 없습니다.");

    }

    @Test
    @DisplayName("발급할 쿠폰이 있을경우")
    void 발금_쿠폰O(){
        // given
        DomainCoupon coupon = DomainCoupon.builder()
                .id(1L)
                .couponNumber(UUID.randomUUID().toString())
                .type(CouponType.FLAT)
                .discountPrice(new BigDecimal(5_000))
                .quantity(100)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 12, 30, 12, 12))
                .build();

        //when
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        DomainCoupon result = service.issueCoupon(1L);

        //then
        assertThat(result.getQuantity()).isEqualTo(99);
        verify(couponRepository, times(1)).update(any(UpdateCoupon.class));

    }

}