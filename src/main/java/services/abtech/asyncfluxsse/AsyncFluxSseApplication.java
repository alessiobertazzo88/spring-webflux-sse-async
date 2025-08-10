package services.abtech.asyncfluxsse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@SpringBootApplication
public class AsyncFluxSseApplication {

        public static void main(String[] args) {
                SpringApplication.run(AsyncFluxSseApplication.class, args);
        }

        @Bean
        public Map<UUID, Sinks.Many<Message>> messageSinks() {
                return new ConcurrentHashMap<>();
        }

}
