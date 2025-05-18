package kr.hhplus.be.server.support;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
public class RedisCleanup {

    private final RedisConnectionFactory factory;

    public RedisCleanup(RedisConnectionFactory factory) {
        this.factory = factory;
    }

    public void flushAll() {
        factory.getConnection().flushAll();
    }
}
