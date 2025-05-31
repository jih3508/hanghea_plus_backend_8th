package kr.hhplus.be.server.common.kafka;

import lombok.Getter;

@Getter
public enum TopicType {

    ORDER("ORDER", "order-events"),;

    private final String code;
    private final String topic;

    TopicType(String code, String topic) {
        this.code = code;
        this.topic = topic;
    }
}
