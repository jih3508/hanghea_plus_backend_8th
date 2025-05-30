package kr.hhplus.be.server.common.kafka;

import lombok.Getter;

@Getter
public enum TopicType {

    ORDER("ORDER", "order-events"),
    COUPON_ISSUE_REQUEST("COUPON_ISSUE_REQUEST", "coupon-issue-request"),
    COUPON_ISSUE_RESULT("COUPON_ISSUE_RESULT", "coupon-issue-result"),;

    private final String code;
    private final String topic;

    TopicType(String code, String topic) {
        this.code = code;
        this.topic = topic;
    }
}
