package io.seqera.events.handler

import com.sun.net.httpserver.HttpExchange
import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import io.seqera.events.dao.EventDao
import groovy.json.JsonSlurper
import io.seqera.events.dto.Event
import java.util.UUID
import java.net.InetAddress
import java.time.LocalDateTime
import io.seqera.events.rateLimiter.RateLimiter
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class EventHandler implements Handler {

    private EventDao eventDao
    private JsonSlurper json
    private UUID handlerId

    EventHandler(EventDao dao){
        this.eventDao = dao
        this.json = new JsonSlurper()
        this.handlerId = UUID.randomUUID();
    }

    @Override
    String getHandlerPath() {
        return "/events"
    }

    @Override
    void handle(HttpExchange http) throws IOException {
        log.debug("handlerId: ${handlerId.toString().substring(0,8)} at ${LocalDateTime.now()}")
        switch (http.requestMethod) {
            case "POST" -> {
                handlePost(http)

            }
            case "GET" -> {
                handleGet(http)

            }
            default -> http.sendResponseHeaders(405, 0)
        }


    }
    void handleGet(HttpExchange http) {
        def events = eventDao.list()
        http.responseHeaders.add("Content-type", "application/json")
        def response = JsonOutput.toJson(events)
        http.sendResponseHeaders(200, response.length())
        http.responseBody.withWriter { out ->
            out << response
        }
    }
    void handlePost(HttpExchange http) {
        def body = http.requestBody.text
        def event = this.json.parseText(body) as Event
        event = this.eventDao.save(event)
        // TODO: encapsulate common flow into super class handling json header and parsing
        http.responseHeaders.add("Content-type", "application/json")
        def response = JsonOutput.toJson(event)
        http.sendResponseHeaders(200, response.length())
        http.responseBody.withWriter { out ->
            out << response
        }
    }
}
