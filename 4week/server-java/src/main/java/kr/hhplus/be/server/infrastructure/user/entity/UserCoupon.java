package kr.hhplus.be.server.infrastructure.user.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.user.model.CreateUserCoupon;
import kr.hhplus.be.server.infrastructure.coupon.entity.Coupon;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "coupon_id")
    @JoinColumn
    private Long couponId;

    @Column(name = "is_used")
    private Boolean isUsed;

    @Column(name = "issued_date_time")
    @CreatedDate
    private LocalDateTime issuedDateTime;

    @Builder
    public UserCoupon(Long userId, Long couponId,  Boolean isUsed) {
        this.userId = userId;
        this.couponId = couponId;
        this.isUsed = isUsed;
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }

    public void usedCoupon() {
        this.isUsed = false;
    }

    public static UserCoupon create(CreateUserCoupon createUserCoupon) {
        return new UserCoupon(createUserCoupon.getUserId(), createUserCoupon.getCouponId(), true);
    }

}
