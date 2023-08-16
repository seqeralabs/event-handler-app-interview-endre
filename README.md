# Event Handler App

Seqera is building the next event handling app
to store all the data coming from [Nextflow](http://nextflow.io)
pipelines execution.

The app has been built to be almost dependency-less
except for JDK, Groovy database driver(s) dependencies along with
testing frameworks: unless differently specified in the issue no additional dependencies
should be explicitly added to the project.

The startup of the process is centralized in the [App.groovy](app/src/main/groovy/App.groovy)
which takes care of:

-   starting the server
-   configuring the [handler](app/src/main/groovy/io/seqera/events/handler/EventHandler.groovy)
-   configuring the [database connection provider](app/src/main/groovy/io/seqera/events/utils/db/ConnectionProvider.groovy)
-   migration of database tables

A minimal request example can be found using the [event.http](event.http) (IntelliJ internal http client tester)

## Configuration

Configuration is located in the [app/src/main/resources/app.yaml](app/src/main/resources/app.yaml)

## Execute locally

Launch the App.groovy main method or use the command `./gradlew app:run` from the root folder

## Dockerized Test Setup explained

To test rate-limiter feature a dockerized setup is provided. The setup consists of several containers:

-   event-handler-app: the main app handling events, several instances are deployed (2 replicas by default) - runs on Java Runtime, written in Groovy
-   nginx: used to load balance between app instances
-   redis: used to store IP-based counts, necessary for rate-limiting
-   load-generator: used to make requests to the app - runs on Node.js, uses the [autocannon](https://github.com/mcollina/autocannon) library

The test setup uses [Docker Compose](https://docs.docker.com/compose/install/) to run a multiple containers. The services and configuration is done the yaml file - docker-compose.testing.yaml. IP addresses are spoofed using docker's IP Address Managements ([ipam](https://docs.docker.com/compose/compose-file/06-networks/#ipam)). Nginx acts as reverse-proxy and does NAT by default so requests going through it will have their IP remapped to the nginx IP. To be able ot preserve the real IP address, we are setting the value of it to the 'X-Real-IP' header in the nginx config. This means that in the event-handler-app we are using the value from the `X-Real-IP` header to rate-limit - this is specific to this load balancing solution and could be changed depending on the set up.

### Executing tests using Dockerized Test Setup

Pre-requisites:

-   having [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed

With only one command executed in the project:

```
docker build . -t event-handler-app -f Dockerfile && docker compose -f docker-compose.testing.yaml down && docker compose -f docker-compose.testing.yaml up --build --force-recreate


# Explanations
# 1. Build event-handler-app image
# docker build . -t event-handler-app -f Dockerfile
# Optional - test image can be run with: docker run --rm -p 8000:8000 event-handler-app
# 2. Run test suit with docker-compose (and remove previous containers)
# docker compose -f docker-compose.testing.yaml down && docker compose -f docker-compose.testing.yaml up --build --force-recreate
# Optional - clean up dangling docker image
# docker image prune
```

Expected test results - with default set-up:

-   load-generator-1-whitelist - will send 3 requests from a whitelisted IP - all of them allowed, 200 HTTP Status code
-   load-generator-2-blacklist - will send 3 requests from a blacklisted IP - all of them rejected, 409 HTTP Status code
-   load-generator-3-rate-exceeded - will send requests during 3 seconds continuously - every second 5 requests should be allowed all others rejected - so in total we expect 3\*5 number of 200 HTTPS Status codes, all other should be 409 HTTP Status code

#### Testing without Redis

Use the `InMemoryRequestCountDao` instead of the `RedisRequestCountDao` when setting up the wiring of the dependencies in the `App.groovy`. This implementation uses an in-memory hashmap, so data is not shared between instance of event-handler-app.

#### Testing with Redis

Event-handler-app started locally with gradle and redis running in docker:

-   set-up redis.conf with the username/password/bind set-up
-   start redis-server with the following command
    -   `docker run --rm -p 6379:6379 -v "absolute_path_to_redis.conf:/redis.conf" redis redis-server /redis.conf`
-   modify app.yaml config for the redis (host = `localhost`, password, port)
-   start application `./gradlew app:run`
