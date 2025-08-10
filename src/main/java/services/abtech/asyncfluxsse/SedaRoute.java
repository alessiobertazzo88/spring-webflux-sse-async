package services.abtech.asyncfluxsse;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class SedaRoute extends RouteBuilder {

    private final SedaToFluxProcessor processor;

    public SedaRoute(SedaToFluxProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void configure() {
        from("seda:input").process(processor);
    }
}
