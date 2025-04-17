package kr.hhplus.be.server.domain.coupon.model;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.user.model.DomainUserCoupon;
import kr.hhplus.be.server.infrastructure.coupon.entity.Coupon;
import kr.hhplus.be.server.infrastructure.coupon.entity.CouponType;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import kr.hhplus.be.server.infrastructure.user.entity.UserCoupon;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class DomainCoupon {

    private Long id;

    private String couponNumber;

    private Integer quantity;

    private CouponType type;

    private Integer rate;

    private BigDecimal discountPrice;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;


    public Boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        return this.startDateTime.isBefore(now) || endDateTime.isAfter(now);
    }

    public Boolean isIssued() {
        return this.quantity > 0;
    }

    public void  issueCoupon() {
        if (!this.isIssued()) {
            throw new ApiExceptionResponse(HttpStatus.BAD_REQUEST, "발급할 쿠폰이 없습니다.");
        }
        this.quantity -= 1;
    }


    public BigDecimal getDiscountPrice(BigDecimal price) {
        if(this.type.equals(CouponType.FLAT)){
            // 고정 금액 할인
            BigDecimal discounted = price.subtract(this.discountPrice);
            return discounted.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : discounted;
        }else if (this.type.equals(CouponType.RATE)) {
            // 퍼센트 할인
            BigDecimal discount = price.multiply(BigDecimal.valueOf(this.rate)).divide(BigDecimal.valueOf(100));
            BigDecimal discounted = price.subtract(discount);
            return discounted.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : discounted;
        }

        return price;
    }











}

