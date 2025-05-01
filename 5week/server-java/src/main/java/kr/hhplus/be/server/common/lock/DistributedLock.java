package kr.hhplus.be.server.common.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    /**
     * 락의 이름
     */
    String key();

    /**
     * 락의 접두사
     */
    String prefix() default "lock";

    /**
     * 락을 기다리는 시간 (기본값 5초)
     */
    long waitTime() default 5L;
    
    /**
     * 락 유지 시간 (기본값 3초)
     */
    long leaseTime() default 3L;
    
    /**
     * 시간 단위 (기본값 SECONDS)
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
