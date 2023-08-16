package io.seqera.events.rateLimiter

class RateLimiterConfig {
    private int timeIntervalInSec
    private int maxRequestsPerIntervalPerIp
    private Set<String> ipWhiteList
    private Set<String> ipBlackList
    private boolean enabled
    
    // default constructor
    RateLimiterConfig(){}
    
    RateLimiterConfig(int timeIntervalInSec, int maxRequestsPerIntervalPerIp, ArrayList<String> ipWhiteList, ArrayList<String> ipBlackList, boolean enabled){
        this.timeIntervalInSec = timeIntervalInSec
        this.maxRequestsPerIntervalPerIp = maxRequestsPerIntervalPerIp
        this.ipWhiteList = new HashSet<>(ipWhiteList)
        this.ipBlackList = new HashSet<>(ipBlackList)
        this.enabled = enabled
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getTimeIntervalInSec() {
        return timeIntervalInSec;
    }

    public int getMaxRequestsPerIntervalPerIp() {
        return maxRequestsPerIntervalPerIp;
    }

    public Set<String> getIpWhiteList() {
        return ipWhiteList;
    }

    public Set<String> getIpBlackList() {
        return ipBlackList;
    }
}