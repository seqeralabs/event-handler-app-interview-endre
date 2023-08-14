package io.seqera.events.filter

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.seqera.events.rateLimiter.RateLimiter
 
@CompileStatic
@Slf4j
public class RateLimiterFilter extends Filter {
 
    private RateLimiter rateLimiter;
    private static final String FILTER_DESC = "Rate Limiter Filter";
 
    RateLimiterFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public String description() {
        return FILTER_DESC;
    }
 
    @Override
    public void doFilter(HttpExchange httpExchange, Filter.Chain chain) throws IOException {   
        if (!rateLimiter.isRequestAllowed(httpExchange)) {
            log.debug("REJECTED")
            httpExchange.sendResponseHeaders(429, -1)
            return
        } 
        log.debug("ACCEPTED")
        chain.doFilter(httpExchange);
    }
}