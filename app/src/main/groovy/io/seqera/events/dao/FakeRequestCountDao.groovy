package io.seqera.events.dao

import groovy.transform.CompileStatic

@CompileStatic
class FakeRequestCountDao implements RequestCountDao {

    private int internalCount = 0

    int incrementAndGetCount(String ip, int timeIntervalInSec) {
        return internalCount++
    }
}
