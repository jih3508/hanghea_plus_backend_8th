package kr.hhplus.be.server.domain.user.model;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class CreateUserCoupon {

    private Long userId;

    private Long couponId;
}
