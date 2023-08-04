package io.seqera.events.utils.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands

class RedisConnectionImpl implements RedisConnection {

    private String host
    private int port
    private String password
    private RedisClient redisClient
    private StatefulRedisConnection<String, String> connection

    RedisConnectionImpl(String host, int port, String password) {
        this.host = host
        this.port = port
        this.password = password                              
    }

    RedisCommands getApiConnection() {
        if (connection == null) {
            def redisUri = RedisURI.Builder.redis(host, port).withPassword(password).build();
            redisClient = RedisClient.create(redisUri);
            connection = redisClient.connect();
        }
        return connection.sync();          
    }

    void closeConnection(){
        if (connection != null) {
            connection.close();
            redisClient.shutdown();
        }
    }
}