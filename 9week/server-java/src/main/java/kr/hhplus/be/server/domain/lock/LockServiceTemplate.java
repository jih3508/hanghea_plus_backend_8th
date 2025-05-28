package kr.hhplus.be.server.domain.lock;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface LockServiceTemplate {

    <T> T executeWithLock(String key, Long waitTime, Long leaseTime, TimeUnit timeUnit, LockCallback<T> callback) throws Throwable;

    <T> T executeWithLockList(List<String> keys, Long waitTime, Long leaseTime, TimeUnit timeUnit, LockCallback<T> callback)throws Throwable;
}
