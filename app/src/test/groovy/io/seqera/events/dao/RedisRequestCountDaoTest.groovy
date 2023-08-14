import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.thenReturn;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.ExpireArgs 
import io.seqera.events.dao.RedisRequestCountDao;

@ExtendWith(MockitoExtension.class)
public class RedisRequestCountDaoTest {
  
  @Test
  public void setExpireAndReturnOne_whenFirstTimeSeenIp(@Mock RedisCommands mockRedisCommands) {
    // Arrange
    String testIp = "10.0.0.75"
    Long testTimeInteraval = 10
    RedisRequestCountDao sut = new RedisRequestCountDao(mockRedisCommands);

    // Act 
    sut.incrementAndGetCount(testIp, 10);    
    
    // Assert
    verify(mockRedisCommands, times(1)).expire(eq(testIp), eq(testTimeInteraval), refEq(ExpireArgs.Builder.nx()));
    verify(mockRedisCommands, times(1)).incr(testIp);
  }
}