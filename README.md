# Spring WebFlux SSE Async

Example application demonstrating how to use Spring Boot, Reactor, and Apache Camel to send asynchronous messages and stream them to clients via **Server‑Sent Events (SSE)**.
Each client connects with a `UUID` and receives a real-time stream of relevant messages.

## Project Purpose

This project provides a simple yet powerful infrastructure to send real-time messages to specific clients via **Server-Sent Events (SSE)**.  
Each client is identified by a `UUID` and will only receive messages addressed to that identifier.  
It is intended for practical use cases where real-time updates are needed without the overhead of WebSockets or constant polling, such as status updates for asynchronous processes or notifications in single-page applications (SPA) today a must-have feature for Generative AI driven applications.

⚠ **Important:** Messages are **not persisted**. A client will only receive a message if it is already connected to `/stream/{uuid}` at the time the message is sent.

## Requirements
- Java 17
- Maven 3 or the bundled `mvnw` script

## Run
Start the application with:

```bash
./mvnw spring-boot:run
```

The application listens on port `8080`.

## Main APIs

**Order of operations is important:** You must first open the SSE stream with `/stream/{uuid}`, then send messages to that same `UUID` using `/enqueue`. If the client is not connected at the time of sending, the message will be lost.

- **POST `/enqueue`** – enqueue a message. The body must contain:
  ```json
  {
    "uuid": "<client-uuid>",
    "body": "message text"
  }
  ```
- **GET `/stream/{uuid}`** – opens an SSE connection that streams messages for the specified `UUID`.

OpenAPI documentation is available at `/swagger-ui.html`.

## Example Usage

1. **Open the SSE connection** (must be done first):
   ```bash
   curl http://localhost:8080/stream/<client-uuid>
   ```

   This keeps the connection open and listens for real-time messages.

2. **Send a message** to the same UUID:
   ```bash
   curl -X POST http://localhost:8080/enqueue \
     -H "Content-Type: application/json" \
     -d '{
       "uuid": "<client-uuid>",
       "body": "Test message"
     }'
   ```

3. **Receive the message**:
   If the SSE connection is open, the message will be delivered immediately.

## Tests
Run the test suite with:

```bash
./mvnw test
```

## Build
To create the executable artifact:

```bash
./mvnw clean package
java -jar target/spring-webflux-sse-async-0.0.1-SNAPSHOT.jar
```

## License
This project is licensed under the MIT License.
You are free to use, modify, and distribute this software, provided that the original copyright and license notice are included in any copies or substantial portions of the software.
