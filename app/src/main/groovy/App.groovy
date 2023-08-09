import com.sun.net.httpserver.HttpServer
import groovy.sql.Sql
import io.seqera.events.dao.EventDao
import io.seqera.events.dao.SqlEventDao
import io.seqera.events.dao.RequestCountDao
import io.seqera.events.dao.RedisRequestCountDao
import io.seqera.events.dao.InMemoryRequestCountDao
import io.seqera.events.handler.EventHandler
import io.seqera.events.handler.Handler
import io.seqera.events.utils.AppContext
import groovy.yaml.YamlSlurper
import io.seqera.events.utils.db.ConnectionProvider
import io.seqera.events.utils.db.ConnectionProviderImpl
import io.seqera.events.utils.redis.RedisConnection
import io.seqera.events.utils.redis.RedisConnectionImpl
import io.seqera.events.rateLimiter.RateLimiter
import io.seqera.events.rateLimiter.CountBasedRateLimiter
import io.seqera.events.rateLimiter.RateLimiterConfig
import groovy.util.logging.Slf4j

@Slf4j
class App {

    static PORT = 8000
    static Handler[] handlers
    static HttpServer httpServer
    static AppContext context
    static ConnectionProvider connectionProvider
    static RedisConnection redisConnection
    static RateLimiterConfig rateLimiterConfig

    static void main(String[] args) {
        context = buildContext()
        
        EventDao eventDao = new SqlEventDao(context.connectionProvider.getConnection())
        RequestCountDao requestCountDao = new RedisRequestCountDao(context.redisConnection.getApiConnection())
        //RequestCountDao requestCountDao = new InMemoryRequestCountDao()
        RateLimiter rateLimiter = new CountBasedRateLimiter(requestCountDao, context.rateLimiterConfig)
        
        handlers = [new EventHandler(eventDao, rateLimiter)]
        httpServer = startServer()
    }


    static AppContext buildContext() {
        def configFile = parseConfigFile('/app.yaml');
        connectionProvider = buildConnectionProvider(configFile)
        rateLimiterConfig = buildRateLimiterConfig(configFile)
        redisConnection = buildRedisConnection(configFile)
        migrateDb()
        return new AppContext(connectionProvider: connectionProvider, redisConnection: redisConnection, rateLimiterConfig: rateLimiterConfig)
    }
    static HttpServer startServer() {
        return HttpServer.create(new InetSocketAddress(PORT), /*max backlog*/ 0).with {
            log.info("Server is listening on ${PORT}, hit Ctrl+C to exit.")
            for (def h : handlers){
                createContext(h.handlerPath, h)
            }
            start()
        }
    }

    static migrateFrom(Sql sql, String migrationFolder){
        def folder = new File(App.classLoader.getResource(migrationFolder).toURI())
        def migrationFiles = folder.listFiles  {it -> it.name.endsWith(".sql")}.sort {Long.parseLong(it)} as File[]
        migrationFiles.each {
            sql.execute(it.text)
        }
    }

    static Object parseConfigFile(String path = '/app.yaml') {
        def file = new File(App.class.getResource('/app.yaml').toURI())
        return new YamlSlurper().parse(file)
    }

    static ConnectionProvider buildConnectionProvider(Object configFile){
        def databaseConfig = configFile['app']['database']
        return new ConnectionProviderImpl(serverUrl: databaseConfig['url'], username: databaseConfig['username'],
                password: databaseConfig['password'], driver: databaseConfig['driver'])
    }

    static RateLimiterConfig buildRateLimiterConfig(Object configFile){
        def conf = configFile['app']['rateLimit']
        return new RateLimiterConfig(conf['timeIntervalInSec'], conf['maxRequestsPerIntervalPerIp'], conf['ipWhiteList'], 
                conf['ipBlackList'], conf['enabled'])
    }


    static RateLimiter buildRateLimiter(RequestCountDao requestCountDao, Object configFile){
        def conf = configFile['app']['rateLimit']
        return new CountBasedRateLimiter(requestCountDao, conf['timeIntervalInSec'], conf['maxRequestsPerIntervalPerIp'], 
                conf['ipWhiteList'], conf['ipBlackList'], conf['enabled'])
    }

    static RedisConnection buildRedisConnection(Object configFile){
        def conf = configFile['app']['redis']
        return new RedisConnectionImpl(conf['host'], conf['port'], conf['password'])
    }

    static def migrateDb() {
        def file = new File(App.class.getResource('/app.yaml').toURI())
        def conf = new YamlSlurper().parse(file)
        def databaseConfig = conf['app']['database']
        def sql  = connectionProvider.getConnection()
        if(databaseConfig['migrations']) {
            migrateFrom(sql, databaseConfig['migrations'] as String)
        }
        return sql
    }
}
