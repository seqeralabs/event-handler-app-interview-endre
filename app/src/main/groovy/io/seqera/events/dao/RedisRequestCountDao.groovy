package io.seqera.events.dao

import groovy.transform.CompileStatic
import io.seqera.events.utils.redis.RedisConnection
import io.lettuce.core.ExpireArgs 
import io.lettuce.core.api.sync.RedisCommands

@CompileStatic
class RedisRequestCountDao implements RequestCountDao {

    private RedisCommands redisApi

    RedisRequestCountDao(RedisCommands redisApi) {
        this.redisApi = redisApi;
    }

    int incrementAndGetCount(String ip, int timeIntervalInSec) {
        redisApi.expire(ip, timeIntervalInSec, ExpireArgs.Builder.nx());
        return redisApi.incr(ip);
    }
}
