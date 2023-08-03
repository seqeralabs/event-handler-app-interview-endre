package io.seqera.events.rateLimiter

import com.sun.net.httpserver.HttpExchange

interface RateLimiter {
    boolean isRequestAllowed(HttpExchange http)
}