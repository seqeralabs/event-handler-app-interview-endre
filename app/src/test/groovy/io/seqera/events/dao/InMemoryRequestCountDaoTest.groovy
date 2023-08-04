import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalTime

import io.seqera.events.dao.InMemoryRequestCountDao;

public class InMemoryRequestCountDaoTest {
  @Test
  public void returnOne_WhenIpSeenForTheFirstTime() {
    // Arrange
    InMemoryRequestCountDao sut = new InMemoryRequestCountDao()
    def ip = 'testIp'
    def testTimeIntervalInSec = 10
    
    // Act & Assert
    def actualCount = sut.incrementAndGetCount(ip,testTimeIntervalInSec)

    //Assert
    assertEquals(actualCount,1);
  }

  @Test
  public void returnIncrement_WhenIpSeenBeforeInsideTimeInterval() {
    // Arrange
    def now = LocalTime.now()
    def testTimeIntervalInSec = 10
    def seenBeforeInsideInterval = now.minusSeconds(testTimeIntervalInSec - 5)
    def ip = 'testIp'
    def beforeCount = 4;
    
    Map<String, LocalTime> ipTimestamps = new HashMap<>();  
    ipTimestamps.put(ip, seenBeforeInsideInterval)

    Map<String, Integer> ipCounts = new HashMap<>();
    ipCounts.put(ip, beforeCount);

    InMemoryRequestCountDao sut = new InMemoryRequestCountDao(ipCounts,ipTimestamps);

    // Act & Assert
    def actualCount = sut.incrementAndGetCount(ip,testTimeIntervalInSec);

    //Assert
    assertEquals(actualCount,beforeCount+1);
  }

  @Test
  public void returnOne_WhenIpSeenBeforeOutsideTimeInterval() {
    // Arrange
    def now = LocalTime.now()
    def testTimeIntervalInSec = 10
    def seenBeforeOutsideInterval = now.minusSeconds(2 * testTimeIntervalInSec)
    def ip = 'testIp'
    def beforeCount = 4;
    
    Map<String, LocalTime> ipTimestamps = new HashMap<>();
    ipTimestamps.put(ip, seenBeforeOutsideInterval);

    Map<String, Integer> ipCounts = new HashMap<>();
    ipCounts.put(ip, beforeCount);

    InMemoryRequestCountDao sut = new InMemoryRequestCountDao(ipCounts,ipTimestamps);

    // Act & Assert
    def actualCount = sut.incrementAndGetCount(ip,testTimeIntervalInSec);
    println "Hello actualCount ${actualCount}"

    //Assert
    assertEquals(actualCount,1);
  }
}
