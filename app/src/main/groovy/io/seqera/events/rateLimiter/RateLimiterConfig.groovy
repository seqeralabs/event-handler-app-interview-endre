package io.seqera.events.rateLimiter

class RateLimiterConfig {
    int timeIntervalInSec
    int maxRequestsPerIntervalPerIp
    Set<String> ipWhiteList
    Set<String> ipBlackList
    boolean enabled
    // default constructor
    RateLimiterConfig(){}
    RateLimiterConfig(int timeIntervalInSec, int maxRequestsPerIntervalPerIp, ArrayList<String> ipWhiteList, ArrayList<String> ipBlackList, boolean enabled){
        this.timeIntervalInSec = timeIntervalInSec
        this.maxRequestsPerIntervalPerIp = maxRequestsPerIntervalPerIp
        this.ipWhiteList = new HashSet<>(ipWhiteList)
        this.ipBlackList = new HashSet<>(ipBlackList)
        this.enabled = enabled
    }
}