package kr.hhplus.be.server.common.event;

import lombok.Getter;

@Getter
public enum EventType {
    ORDER("ORDER"),;

    private final String code;

    EventType(String code) {
        this.code = code;
    }

}
