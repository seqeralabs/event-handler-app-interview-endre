package io.seqera.events.dao

import groovy.transform.CompileStatic
import java.time.LocalTime

@CompileStatic
class InMemoryRequestCountDao implements RequestCountDao {

    private Map<String, Integer> ipCounts = new HashMap<>()
    private Map<String, LocalTime> ipTimestamps = new HashMap<>()

    InMemoryRequestCountDao(){
        this.ipCounts = new HashMap<>()
        this.ipTimestamps = new HashMap<>()
    }

    InMemoryRequestCountDao(Map<String, Integer> ipCounts, Map<String, LocalTime> ipTimestamps){
        this.ipCounts = ipCounts
        this.ipTimestamps = ipTimestamps
    }
    
    int incrementAndGetCount(String ip, int timeIntervalInSec) {
        def now = LocalTime.now()
        def ipFirstSeenInTimeInterval = ipTimestamps.getOrDefault(ip, now);
        def rateLimitIntervalEnds = ipFirstSeenInTimeInterval.plusSeconds(timeIntervalInSec)

        if (ipFirstSeenInTimeInterval.equals(now) || rateLimitIntervalEnds.isBefore(now)) {
            println "Resetting counter"
            ipCounts.put(ip, 1);
            ipTimestamps.put(ip, now)
            return 1
        } else {
            println "Increasing counter"
            def count = ipCounts.getOrDefault(ip, 0);
            count++;
            ipCounts.put(ip, count);
            return count
        } 
    }
}
