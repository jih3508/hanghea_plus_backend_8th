package kr.hhplus.be.server.common.lock;

import lombok.Getter;

@Getter
public enum LockType {

    STOCK("STOCK", "STOCK:"), // 재고
    PRODUCT("PRODUCT", "PRODUCT:"),
    COUPON("COUPON", "COUPON:"), // 쿠폰
    POINT("POINT", "POINT:"); // 포인트


    private final String code;

    private final String lockName;

    LockType(String code, String lockName) {
        this.code = code;
        this.lockName = lockName;
    }
}
