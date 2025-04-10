package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.entity.CouponType;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.user.service.UserCouponService;
import kr.hhplus.be.server.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponFacadeTest {

    @InjectMocks
    private CouponFacade facade;

    @Mock
    private CouponService service;

    @Mock
    private UserService userService;

    @Mock
    private UserCouponService userCouponService;

    @Mock
    private CouponService couponService;

    @Test
    @DisplayName("발급시 없는 회원 처리")
    void 발급_없는_회원(){

        CouponIssueCommand command = mock(CouponIssueCommand.class);
        //given, when
        when(userService.findById(anyLong())).thenThrow(new ApiExceptionResponse(HttpStatus.NOT_FOUND, "없는 사용자 입니다."));

        // then
        assertThatThrownBy(()-> facade.issue(command)).isInstanceOf(ApiExceptionResponse.class);
        verify(service, never()).issueCoupon(anyLong());
        verify(userCouponService, never()).save(any(),any());
    }

    @Test
    @DisplayName("쿠폰 발급 개수가 보족해서 오류가 날 경우")
    void 쿠폰_개수_부족(){

        // given
        CouponIssueCommand command = CouponIssueCommand.builder()
                .userId(1L)
                .couponId(1L)
                .build();

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
                .quantity(0)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 3, 30, 12, 12))
                .build();

        // when
        when(userService.findById(1l)).thenReturn(user);
        when(service.issueCoupon(1L)).thenThrow(new ApiExceptionResponse(HttpStatus.BAD_REQUEST, "발급할 쿠폰이 없습니다."));

        // then
        assertThatThrownBy(()-> facade.issue(command)).isInstanceOf(ApiExceptionResponse.class);
        verify(userCouponService, never()).save(any(),any());

    }

    @Test
    @DisplayName("쿠폰 정상 발급")
    void 쿠폰_발급(){
        // given
        CouponIssueCommand command = CouponIssueCommand.builder()
                .userId(1L)
                .couponId(1L)
                .build();

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
                .quantity(1000)
                .startDateTime(LocalDateTime.of(2025, 1, 30, 0, 0))
                .endDateTime(LocalDateTime.of(2025, 3, 30, 12, 12))
                .build();


        // when
        when(userService.findById(1l)).thenReturn(user);
        when(service.issueCoupon(1L)).thenReturn(coupon);
        facade.issue(command);

        // then
        verify(userCouponService, times(1)).save(user, coupon);

    }
}