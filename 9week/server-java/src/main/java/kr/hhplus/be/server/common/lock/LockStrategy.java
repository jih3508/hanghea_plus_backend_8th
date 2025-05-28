package kr.hhplus.be.server.common.lock;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LockStrategy {

    SIMPLE_LOCK("심플 락"),
    SPIN_LOCK("스핀 락"),
    PUB_SUB_LOCK("pub/sub lock"),;

    private final String description;
}
