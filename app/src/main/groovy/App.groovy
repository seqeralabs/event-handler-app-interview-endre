import com.sun.net.httpserver.HttpServer
import groovy.sql.Sql
import groovy.transform.CompileStatic
import io.seqera.events.dao.EventDao
import io.seqera.events.dao.SqlEventDao
import io.seqera.events.dao.RequestCountDao
import io.seqera.events.dao.RedisRequestCountDao
import io.seqera.events.dao.InMemoryRequestCountDao
import io.seqera.events.handler.EventHandler
import io.seqera.events.handler.Handler
import io.seqera.events.utils.AppContext
import groovy.yaml.YamlSlurper
import io.seqera.events.utils.config.AppConfig
import io.seqera.events.utils.db.ConnectionProvider
import io.seqera.events.utils.db.ConnectionProviderImpl
import io.seqera.events.utils.redis.RedisConnection
import io.seqera.events.utils.redis.RedisConnectionImpl
import io.seqera.events.rateLimiter.RateLimiter
import io.seqera.events.rateLimiter.RateLimiterConfig
import io.seqera.events.rateLimiter.CountBasedRateLimiter
import io.seqera.events.rateLimiter.RateLimiterConfig
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class App {

    static Integer PORT = 8000
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
        AppConfig configFile = new AppConfig('/app.yaml');
        connectionProvider = configFile.buildDatabaseConnectionProvider()
        rateLimiterConfig = configFile.buildRateLimiterConfig()
        redisConnection = configFile.buildRedisConnection()
        migrateDb(configFile.getDBConfig())
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
        def migrationFiles = folder.listFiles {it -> it.name.endsWith(".sql")}.sort() as File[]
        migrationFiles.each {
            sql.execute(it.text)
        }
    }

    static Object parseConfigFile(String path = '/app.yaml') {
        def file = new File(App.class.getResource('/app.yaml').toURI())
        return new YamlSlurper().parse(file)
    }

    static def migrateDb(Object dbConfig) {
        def sql  = connectionProvider.getConnection()
        if(dbConfig['migrations']) {
            migrateFrom(sql, dbConfig['migrations'] as String)
        }
        return sql
    }
}
