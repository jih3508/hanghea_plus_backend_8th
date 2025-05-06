package kr.hhplus.be.server.common.lock;

import java.util.function.Supplier;

/**
 * 분산락(Distributed Lock) 구현을 위한 인터페이스
 * 세 가지 구현체(Simple Lock, Spin Lock, Redisson)에서 공통적으로 사용되는 메서드 정의
 */
public interface DistributedLock {
    
    /**
     * 락을 획득하고 실행 결과를 반환하는 메서드
     * 
     * @param key 락 식별자
     * @param timeoutMillis 락 타임아웃 시간(밀리초)
     * @param supplier 락 획득 후 실행할 로직
     * @return 실행 결과값
     * @param <T> 반환 타입
     */
    <T> T executeWithLock(String key, long timeoutMillis, Supplier<T> supplier);
    
    /**
     * 락을 획득하고 로직을 실행하는 메서드 (반환값 없음)
     * 
     * @param key 락 식별자
     * @param timeoutMillis 락 타임아웃 시간(밀리초)
     * @param runnable 락 획득 후 실행할 로직
     */
    void executeWithLock(String key, long timeoutMillis, Runnable runnable);
}