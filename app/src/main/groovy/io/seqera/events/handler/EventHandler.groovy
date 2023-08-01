package io.seqera.events.handler

import com.sun.net.httpserver.HttpExchange
import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import io.seqera.events.dao.EventDao
import groovy.json.JsonSlurper
import io.seqera.events.dto.Event
import java.util.UUID
import java.net.InetAddress

@CompileStatic
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
        println "handlerId: ${handlerId}"
        String ip = extractRelevantIpAddress(http)
        if (shouldBlockIp(ip)) {
            println "REJECTED"
            http.sendResponseHeaders(429, -1)
            return
        }
        println "ACCEPTED"

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

    String extractRelevantIpAddress(HttpExchange http) {
        InetAddress requestIPInet = http.getRemoteAddress().getAddress()
        String requestIP = requestIPInet.getHostAddress()
        println "Request IP address: ${requestIP}"
        def headerIPList = http.getRequestHeaders().get("X-Real-IP")
        String headerIP = headerIPList.size() > 0 ? headerIPList[0] : null
        println "X-Real-IP header: ${headerIP}"
        return headerIP
    }

    boolean shouldBlockIp(String ip) {
        // Fake test implementation, which blocks even IPs
        String[] octets = ip.split("\\.") // the dot "." needs to be escaped in the regex expression
        String lastOctet = octets[octets.length - 1]
        return lastOctet.toInteger() % 2 == 0
    }
}
