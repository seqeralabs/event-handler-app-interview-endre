import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.thenReturn;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalTime
import org.mockito.Mock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import io.seqera.events.rateLimiter.RateLimiter
import io.seqera.events.filter.RateLimiterFilter;

@ExtendWith(MockitoExtension.class)
public class RateLimiterFilterTest {
  
  @Test
  public void reject_whenRateLimiterDoesNotAllowIp(@Mock RateLimiter mockRateLimiter, @Mock HttpExchange mockHttpExchange, @Mock Filter.Chain mockFilterChain) {
    // Arrange
    when(mockRateLimiter.isRequestAllowed(mockHttpExchange)).thenReturn(false);
    RateLimiterFilter sut = new RateLimiterFilter(mockRateLimiter);

    // Act 
    sut.doFilter(mockHttpExchange, mockFilterChain);    
    
    // Assert
    verify(mockHttpExchange, times(1)).sendResponseHeaders(429,-1);
  }

  @Test
  public void accept_whenRateLimiterAllowsIp(@Mock RateLimiter mockRateLimiter, @Mock HttpExchange mockHttpExchange, @Mock Filter.Chain mockFilterChain) {
    // Arrange
    when(mockRateLimiter.isRequestAllowed(mockHttpExchange)).thenReturn(true);
    RateLimiterFilter sut = new RateLimiterFilter(mockRateLimiter);

    // Act 
    sut.doFilter(mockHttpExchange, mockFilterChain);    
    
    // Assert
    verify(mockFilterChain, times(1)).doFilter(mockHttpExchange);
  }
}