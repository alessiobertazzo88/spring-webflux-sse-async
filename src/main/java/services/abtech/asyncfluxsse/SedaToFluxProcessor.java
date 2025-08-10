package services.abtech.asyncfluxsse;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.util.concurrent.Queues;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SedaToFluxProcessor implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(SedaToFluxProcessor.class);

    private final Map<UUID, Sinks.Many<Message>> sinks;
    private final AtomicLong emissionFailureCount = new AtomicLong();

    public SedaToFluxProcessor(Map<UUID, Sinks.Many<Message>> sinks) {
        this.sinks = sinks;
    }

    @Override
    public void process(Exchange exchange) {
        Message message = exchange.getIn().getBody(Message.class);
        Sinks.Many<Message> messageMany = sinks.computeIfAbsent(message.uuid(), k -> Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false));
        EmitResult result = messageMany.tryEmitNext(message);
        if (result.isFailure()) {
            long failures = emissionFailureCount.incrementAndGet();
            logger.warn("Emission failed ({} total): {}", failures, result);
            messageMany.emitNext(message, (signalType, emitResult) -> {
                logger.error("Retry emission failed: signalType={} result={}", signalType, emitResult);
                return false;
            });
        }
    }
}
