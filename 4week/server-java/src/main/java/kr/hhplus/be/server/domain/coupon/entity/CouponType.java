package kr.hhplus.be.server.domain.coupon.entity;


public enum CouponType {

    FLAT ("FLAT", "정액"),
    RATE("RATE", "정률");


    private final String code;

    private final String description;

    CouponType(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
