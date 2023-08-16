package io.seqera.events.utils.config

import groovy.yaml.YamlSlurper
import io.seqera.events.utils.db.ConnectionProvider
import io.seqera.events.utils.db.ConnectionProviderImpl
import io.seqera.events.utils.redis.RedisConnection
import io.seqera.events.utils.redis.RedisConnectionImpl
import io.seqera.events.rateLimiter.RateLimiterConfig

class AppConfig {

    private Object configFile

    AppConfig(String path='/app.yaml') {
        def file = new File(App.class.getResource(path).toURI())
        this.configFile = new YamlSlurper().parse(file)
    }

    ConnectionProvider buildDatabaseConnectionProvider(){
        def databaseConfig = configFile['app']['database']
        return new ConnectionProviderImpl(serverUrl: databaseConfig['url'], username: databaseConfig['username'],
                password: databaseConfig['password'], driver: databaseConfig['driver'])
    }

    RateLimiterConfig buildRateLimiterConfig(){
        def conf = configFile['app']['rateLimit']
        return new RateLimiterConfig(timeIntervalInSec: conf['timeIntervalInSec'], maxRequestsPerIntervalPerIp: conf['maxRequestsPerIntervalPerIp'], 
                ipWhiteList: conf['ipWhiteList'], ipBlackList: conf['ipBlackList'], enabled: conf['enabled'])
    }

    RedisConnection buildRedisConnection(){
        def conf = configFile['app']['redis']
        return new RedisConnectionImpl(conf['host'], conf['port'], conf['password'])
    }

    Object getDBConfig(){
        return configFile['app']['database']
    }
}