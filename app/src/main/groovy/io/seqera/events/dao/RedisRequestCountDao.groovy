package io.seqera.events.dao

import groovy.transform.CompileStatic
import io.seqera.events.utils.redis.RedisConnection
import io.lettuce.core.ExpireArgs 
import io.lettuce.core.api.sync.RedisCommands
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class RedisRequestCountDao implements RequestCountDao {

    private RedisCommands redisApi

    RedisRequestCountDao(RedisCommands redisApi) {
        this.redisApi = redisApi;
    }

    int incrementAndGetCount(String ip, int timeIntervalInSec) {
        Long value = redisApi.incr(ip);
        log.debug("Counter for ${ip} is ${value}")
        boolean resp = redisApi.expire(ip, timeIntervalInSec, ExpireArgs.Builder.nx());
        log.debug("Expiry timeout for ${ip} was ${resp ? 'Set' : 'Not set'}")
        return value.intValue()
    }
}
