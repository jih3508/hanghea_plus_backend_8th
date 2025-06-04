package kr.hhplus.be.server.domain.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class UpdateUserCoupon {

    private Long userId;

    private Long couponId;

    private Boolean isUsed;
}
