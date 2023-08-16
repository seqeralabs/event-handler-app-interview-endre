import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.seqera.events.dao.RedisRequestCountDao

@Testcontainers
class RedisRequestCountDaoContainerTest {

    @Container
    public GenericContainer redis = new GenericContainer(DockerImageName.parse("redis"))
        .withExposedPorts(6379)
        .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1))

    private RedisClient client
    private StatefulRedisConnection<String,String> redisConnection

    @BeforeEach
    public void setUp() {
        def uri = RedisURI.create(redis.getHost(),redis.getFirstMappedPort())
        client = RedisClient.create(uri)
        redisConnection = client.connect()
    }

    @Test
    void counterExpiresAfterTTL() {
        RedisRequestCountDao counter = new RedisRequestCountDao(redisConnection.sync())

        def initialCount = counter.incrementAndGetCount('ip',1)
        sleep(2_000) // enough time  for the 'ip' key to expire
        def countAfterTTL = counter.incrementAndGetCount('ip', 1)

        assertEquals(1, initialCount)
        assertEquals(1, countAfterTTL,"counter has been reset after one second")
    }

    @Test
    void counterDoesNotExpireBeforeTTL() {
        RedisRequestCountDao counter = new RedisRequestCountDao(redisConnection.sync())

        def initialCount = counter.incrementAndGetCount('ip',3)
        sleep(2_000) // less time then expiry interval
        def secondCount = counter.incrementAndGetCount('ip', 3)

        assertEquals(1, initialCount)
        assertEquals(2, secondCount)
    }


    @AfterEach
    void tearDown() {
        redisConnection.close()
        client.shutdown()
    }
}