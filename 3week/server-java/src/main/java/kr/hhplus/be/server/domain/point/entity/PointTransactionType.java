package kr.hhplus.be.server.domain.point.entity;

import lombok.Getter;

@Getter
public enum PointTransactionType {
    CHARGE("CHARGE", "충전"),
    USE("USE", "사용");

    private final String code;

    private final String description;

    PointTransactionType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
