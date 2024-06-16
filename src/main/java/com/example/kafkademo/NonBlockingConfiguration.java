package com.example.kafkademo;

import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.retrytopic.DeadLetterPublishingRecovererFactory;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationSupport;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.backoff.FixedBackOff;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

@EnableScheduling
//@Configuration
public class NonBlockingConfiguration extends RetryTopicConfigurationSupport {

    @Override
    protected void configureBlockingRetries(BlockingRetriesConfigurer blockingRetries) {
        blockingRetries
                .retryOn(IllegalStateException.class)
                .backOff(new FixedBackOff(2_000, 3));
    }

    @Override
    protected void manageNonBlockingFatalExceptions(List<Class<? extends Throwable>> nonBlockingFatalExceptions) {
        nonBlockingFatalExceptions.add(IllegalStateException.class);
        nonBlockingFatalExceptions.add(IllegalArgumentException.class);
    }

    @Override
    protected Consumer<DeadLetterPublishingRecovererFactory> configureDeadLetterPublishingContainerFactory() {
        return dlprf -> dlprf.setDeadLetterPublishingRecovererCustomizer(dlprc -> dlprc.addHeadersFunction((consumerRecord, exception) -> new RecordHeaders().add("customExceptionHeader", "example".getBytes(StandardCharsets.UTF_8))));
    }
}
