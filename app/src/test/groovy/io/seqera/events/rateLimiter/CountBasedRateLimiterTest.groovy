import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;

import io.seqera.events.dao.RequestCountDao;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.thenReturn;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import io.seqera.events.rateLimiter.RateLimiterConfig;
import io.seqera.events.rateLimiter.CountBasedRateLimiter;

@ExtendWith(MockitoExtension.class)
public class CountBasedRateLimiterTest {
  @Test
  public void whenDisabled_Allow(@Mock RequestCountDao requestCountDao, @Mock RateLimiterConfig rateLimiterConfig, @Mock HttpExchange httpExchange) {
    // Arrange
    when(rateLimiterConfig.isEnabled()).thenReturn(false);
    CountBasedRateLimiter sut = new CountBasedRateLimiter(requestCountDao, rateLimiterConfig);

    // Act & Assert
    assertTrue(sut.isRequestAllowed(httpExchange));
  }

  @Test
  public void whenMissingHeaders_Reject(@Mock RequestCountDao requestCountDao, @Mock RateLimiterConfig rateLimiterConfig, @Mock HttpExchange httpExchange) {
    // Arrange
    when(rateLimiterConfig.isEnabled()).thenReturn(true);
    CountBasedRateLimiter sut = new CountBasedRateLimiter(requestCountDao, rateLimiterConfig);

    // Act & Assert
    assertFalse(sut.isRequestAllowed(httpExchange));
  }

  @Test
  public void whenMissingXRealIpHeader_Reject(@Mock RequestCountDao requestCountDao, @Mock RateLimiterConfig rateLimiterConfig, @Mock HttpExchange httpExchange) {
    // Arrange
    when(rateLimiterConfig.isEnabled()).thenReturn(true);
    CountBasedRateLimiter sut = new CountBasedRateLimiter(requestCountDao, rateLimiterConfig);

    def headers = new Headers()
    headers.add("Content-type", "application/json")
    when(httpExchange.getRequestHeaders()).thenReturn(headers);

    // Act & Assert
    assertFalse(sut.isRequestAllowed(httpExchange));
  }

  @Test
  public void whenBlackListedIp_Reject(@Mock RequestCountDao requestCountDao, @Mock RateLimiterConfig rateLimiterConfig, @Mock HttpExchange httpExchange) {
    // Arrange
    String blackListedIp = "1.1.1.1";
    when(rateLimiterConfig.isEnabled()).thenReturn(true);
    when(rateLimiterConfig.getIpBlackList()).thenReturn(new HashSet<String>(Arrays.asList(blackListedIp)));

    CountBasedRateLimiter sut = new CountBasedRateLimiter(requestCountDao, rateLimiterConfig);

    def headers = new Headers()
    headers.add("X-Real-IP", blackListedIp)
    when(httpExchange.getRequestHeaders()).thenReturn(headers);

    // Act & Assert
    assertFalse(sut.isRequestAllowed(httpExchange));
  }

  @Test
  public void whenWhiteListedIp_Allow(@Mock RequestCountDao requestCountDao, @Mock RateLimiterConfig rateLimiterConfig, @Mock HttpExchange httpExchange) {
    // Arrange
    String whiteListedIp = "2.2.2.2";
    when(rateLimiterConfig.isEnabled()).thenReturn(true);
    when(rateLimiterConfig.getIpWhiteList()).thenReturn(new HashSet<String>(Arrays.asList(whiteListedIp)));

    CountBasedRateLimiter sut = new CountBasedRateLimiter(requestCountDao, rateLimiterConfig);

    def headers = new Headers()
    headers.add("X-Real-IP", whiteListedIp)
    when(httpExchange.getRequestHeaders()).thenReturn(headers);

    // Act & Assert
    assertTrue(sut.isRequestAllowed(httpExchange));
  }

  @Test
  public void whenBelowRateLimit_Allow(@Mock RequestCountDao requestCountDao, @Mock RateLimiterConfig rateLimiterConfig, @Mock HttpExchange httpExchange) {
    // Arrange
    String ip = "3.3.3.3";
    when(rateLimiterConfig.isEnabled()).thenReturn(true);
    when(requestCountDao.incrementAndGetCount(eq(ip), anyInt())).thenReturn(4);
    when(rateLimiterConfig.getMaxRequestsPerIntervalPerIp()).thenReturn(5);
    CountBasedRateLimiter sut = new CountBasedRateLimiter(requestCountDao, rateLimiterConfig);

    def headers = new Headers()
    headers.add("X-Real-IP", ip)
    when(httpExchange.getRequestHeaders()).thenReturn(headers);

    // Act & Assert
    assertTrue(sut.isRequestAllowed(httpExchange));
  }

  @Test
  public void whenAboveRateLimit_Reject(@Mock RequestCountDao requestCountDao, @Mock RateLimiterConfig rateLimiterConfig, @Mock HttpExchange httpExchange) {
    // Arrange
    String ip = "4.4.4.4";
    when(rateLimiterConfig.isEnabled()).thenReturn(true);
    when(requestCountDao.incrementAndGetCount(eq(ip), anyInt())).thenReturn(6);
    when(rateLimiterConfig.getMaxRequestsPerIntervalPerIp()).thenReturn(5);
    CountBasedRateLimiter sut = new CountBasedRateLimiter(requestCountDao, rateLimiterConfig);

    def headers = new Headers()
    headers.add("X-Real-IP", ip)
    when(httpExchange.getRequestHeaders()).thenReturn(headers);

    // Act & Assert
    assertFalse(sut.isRequestAllowed(httpExchange));
  }
}
