package io.seqera.events.rateLimiter

import groovy.transform.CompileStatic
import com.sun.net.httpserver.HttpExchange
import io.seqera.events.dao.RequestCountDao
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class CountBasedRateLimiter implements RateLimiter {
    
    private RequestCountDao requestCountDao
    private RateLimiterConfig config

    CountBasedRateLimiter(RequestCountDao requestCountDao, RateLimiterConfig config){
        this.requestCountDao = requestCountDao;
        this.config = config
    }

    boolean isRequestAllowed(HttpExchange http) {
        if (!config.isEnabled()) {
            return true
        }

        def headers = http.getRequestHeaders()
        if (headers == null || headers.get("X-Real-IP").size() == 0) {
            return false
        }
        def ip = headers.get("X-Real-IP")[0]
        
        if (config.getIpBlackList().contains(ip)) {
            log.debug("BLACKLISTED")
            return false
        } else if (config.getIpWhiteList().contains(ip)) {
            log.debug("WHITELISTED")
            return true
        }

        def count = requestCountDao.incrementAndGetCount(ip, config.getTimeIntervalInSec());
        log.debug("For ${ip} count is ${count}")
        if (count > config.getMaxRequestsPerIntervalPerIp()){
            log.debug("RATE LIMIT EXCEEDED")
            return false
        }

        return true
    }
}