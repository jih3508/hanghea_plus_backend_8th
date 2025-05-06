package kr.hhplus.be.server.common.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking methods that require distributed locking
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLockable {
    /**
     * Expression to generate the lock key. SpEL supported.
     */
    String key();
    
    /**
     * Lock timeout in milliseconds
     */
    long timeoutMillis() default 5000L;
    
    /**
     * Type of distributed lock to use
     */
    DistributedLockType lockType() default DistributedLockType.REDISSON;
}
