package kr.hhplus.be.server.infrastructure.user.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @Column(name = "user_id")
    private User user;

    @ManyToOne
    @Column(name = "coupon_id")
    private Coupon coupon;

    @Column(name = "is_used")
    private Boolean isUsed;

    @Builder
    public UserCoupon(User user, Coupon coupon, Boolean isUsed) {
        this.user = user;
        this.coupon = coupon;
        this.isUsed = isUsed;
    }

    public void usedCoupon() {
        this.isUsed = false;
    }

}
