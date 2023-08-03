package io.seqera.events.utils.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands

class RedisConnectionImpl implements RedisConnection {

    private RedisClient redisClient
    private StatefulRedisConnection<String, String> connection

    RedisConnectionImpl(String host, int port, String password) {
        def redisUri = RedisURI.Builder.redis(host, port).withPassword(password).build();
        this.redisClient = RedisClient.create(redisUri);
        this.connection = this.redisClient.connect();                                 
    }

    RedisCommands getSyncApi() {
        return this.connection.sync();          
    }

    void closeConnection(){
        this.connection.close();
        this.redisClient.shutdown();
    }
}