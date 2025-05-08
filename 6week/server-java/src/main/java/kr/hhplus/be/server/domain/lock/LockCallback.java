package kr.hhplus.be.server.domain.lock;

@FunctionalInterface
public interface LockCallback<T> {

    T doInLock() throws Throwable;
}
