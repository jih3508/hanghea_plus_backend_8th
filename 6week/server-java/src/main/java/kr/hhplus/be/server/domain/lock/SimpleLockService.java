package kr.hhplus.be.server.domain.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimpleLockService implements LockService {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public <T> T executeWithLock(String key, Long waitTime, Long leaseTime, TimeUnit timeUnit ,Supplier<T> supplier) {

        boolean acquired = false;
        try {
            log.debug("락 획득 시도: {}", key);
            // SETNX 명령어로 락 획득 시도 (값이 있으면 실패, 없으면 성공)
            acquired = lock(key, leaseTime, timeUnit);

            if (!acquired) {
                throw new IllegalStateException("Failed to acquire Simple lock for key: " + key);
            }

            return supplier.get();
        }finally{
            // 락 해제
            if (acquired) {
                unLock(key);
            }
        }

    }

    @Override
    public <T> T executeWithLockList(List<String> keys, Long waitTime, Long leaseTime, TimeUnit timeUnit, Supplier<T> supplier) {

        boolean acquired = false;
        for(String key : keys) {
            try {

                log.debug("락 획득 시도: {}", key);
                // SETNX 명령어로 락 획득 시도 (값이 있으면 실패, 없으면 성공)
                acquired = lock(key, leaseTime, timeUnit);

                if (!acquired) {
                    throw new IllegalStateException("Failed to acquire Simple lock for key: " + key);
                }
            }finally {
                // 락 해제
                if (acquired) {
                    unLock(key);
                }
            }
        }

        return supplier.get();
    }

    private boolean lock(String key, Long leaseTime, TimeUnit timeUnit) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue()
                .setIfAbsent(key, String.valueOf(Thread.currentThread().getId()), leaseTime, timeUnit));
    }

    private void unLock(String key){
        redisTemplate.delete(key);
    }
}
