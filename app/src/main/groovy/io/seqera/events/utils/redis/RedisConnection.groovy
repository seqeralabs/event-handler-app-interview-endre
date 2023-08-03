package io.seqera.events.utils.redis

import io.lettuce.core.api.sync.RedisCommands

interface RedisConnection {

    RedisCommands getSyncApi()      
    void closeConnection()
}