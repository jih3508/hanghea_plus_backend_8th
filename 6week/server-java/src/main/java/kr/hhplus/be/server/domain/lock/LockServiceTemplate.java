package kr.hhplus.be.server.domain.lock;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface LockService {

    <T> T executeWithLock(String key, Long waitTime, Long leaseTime, TimeUnit timeUnit, Supplier<T> supplier);

    <T> T executeWithLockList(List<String> keys, Long waitTime, Long leaseTime, TimeUnit timeUnit, Supplier<T> supplier);
}
