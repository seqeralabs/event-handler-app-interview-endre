package io.seqera.events.dao

interface RequestCountDao {
    int incrementAndGetCount(String ip, int timeIntervalInSec)
}