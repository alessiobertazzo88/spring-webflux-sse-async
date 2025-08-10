package services.abtech.asyncfluxsse;

import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.UUID;

@RestController
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    private final ProducerTemplate producerTemplate;
    private final Map<UUID, Sinks.Many<Message>> sinks;

    public MessageController(ProducerTemplate producerTemplate, Map<UUID, Sinks.Many<Message>> sinks) {
        this.producerTemplate = producerTemplate;
        this.sinks = sinks;
    }

    @PostMapping("/enqueue")
    public Mono<Void> enqueue(@RequestBody Message message) {
        return Mono.fromRunnable(() -> {
            sinks.computeIfAbsent(message.uuid(), k -> Sinks.many().multicast().onBackpressureBuffer());
            producerTemplate.sendBody("seda:input", message);
        });
    }

    @GetMapping(value = "/stream/{uuid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(@PathVariable UUID uuid) {
        Sinks.Many<Message> sink = sinks.computeIfAbsent(uuid, k -> Sinks.many().multicast().onBackpressureBuffer());
        return sink.asFlux()
                .map(m -> ServerSentEvent.builder(m.body()).build())
                .doOnCancel(() -> {
                    sinks.remove(uuid);
                    logger.info("Client {} disconnected", uuid);
                });
    }
}
