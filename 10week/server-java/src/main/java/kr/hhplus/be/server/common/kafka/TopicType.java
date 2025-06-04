package kr.hhplus.be.server.common.kafka;

import lombok.Getter;

@Getter
public enum TopicType {

    ORDER("ORDER", "order-events"),
    COUPON_ISSUE_REQUEST("COUPON_ISSUE_REQUEST", "coupon-issue-request"),
    COUPON_ISSUE_RESULT("COUPON_ISSUE_RESULT", "coupon-issue-result"),
    
    // 주문 사가 패턴 관련 토픽들
    STOCK_DEDUCTION_REQUEST("STOCK_DEDUCTION_REQUEST", "stock-deduction-request"),
    STOCK_DEDUCTION_RESULT("STOCK_DEDUCTION_RESULT", "stock-deduction-result"),
    COUPON_USAGE_REQUEST("COUPON_USAGE_REQUEST", "coupon-usage-request"),
    COUPON_USAGE_RESULT("COUPON_USAGE_RESULT", "coupon-usage-result"),
    POINT_DEDUCTION_REQUEST("POINT_DEDUCTION_REQUEST", "point-deduction-request"),
    POINT_DEDUCTION_RESULT("POINT_DEDUCTION_RESULT", "point-deduction-result"),
    COMPENSATION_REQUEST("COMPENSATION_REQUEST", "compensation-request"),
    ORDER_NOTIFICATION("ORDER_NOTIFICATION", "order-notification"),;

    private final String code;
    private final String topic;

    TopicType(String code, String topic) {
        this.code = code;
        this.topic = topic;
    }
}
