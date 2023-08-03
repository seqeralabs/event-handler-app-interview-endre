package io.seqera.events.rateLimiter

import groovy.transform.CompileStatic
import com.sun.net.httpserver.HttpExchange
import io.seqera.events.dao.RequestCountDao

@CompileStatic
class CountBasedRateLimiter implements RateLimiter {
    
    private RequestCountDao requestCountDao
    private RateLimiterConfig config

    CountBasedRateLimiter(RequestCountDao requestCountDao, RateLimiterConfig config){
        this.requestCountDao = requestCountDao;
        this.config = config
    }

    boolean isRequestAllowed(HttpExchange http) {
        if (!config.enabled) {
            return true
        }

        def headers = http.getRequestHeaders()
        if (headers == null || headers.get("X-Real-IP").size() == 0) {
            return false
        }
        def ip = headers.get("X-Real-IP")[0]
        
        if (config.ipBlackList.contains(ip)) {
            println "BLACKLISTED"
            return false
        } else if (config.ipWhiteList.contains(ip)) {
            println "WHITELISTED"
            return true
        }

        def count = requestCountDao.incrementAndGetCount(ip, config.timeIntervalInSec);
        println "For ${ip} count is ${count}"
        if (count > config.maxRequestsPerIntervalPerIp){
            println "RATE LIMIT EXCEEDED"
            return false
        }

        return true
    }
}