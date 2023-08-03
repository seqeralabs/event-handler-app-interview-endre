package io.seqera.events.utils

import groovy.transform.CompileStatic
import io.seqera.events.utils.db.ConnectionProvider
import io.seqera.events.rateLimiter.RateLimiterConfig
import io.seqera.events.utils.redis.RedisConnection

@CompileStatic
class AppContext {

    ConnectionProvider connectionProvider
    RedisConnection redisConnection
    RateLimiterConfig rateLimiterConfig

}
