package com.example.kafkademo;


import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.retrytopic.DeadLetterPublishingRecovererFactory;
import org.springframework.kafka.retrytopic.DestinationTopicResolver;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ExceptionConfiguration {


//    @Bean
//    public DeadLetterPublishingRecoverer recoverer(KafkaOperations<DocumentInfoKey, DocumentInfoValue> template) {
//        return new DeadLetterPublishingRecoverer(template,
//                (r, e) -> new TopicPartition(r.topic() + ".custom.DLT", r.partition()));
//    }
//
//    @Bean
//    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
////        ExponentialBackOffWithMaxRetries bo = new ExponentialBackOffWithMaxRetries(6);
////        bo.setInitialInterval(1_000L);
////        bo.setMultiplier(2.0);
////        bo.setMaxInterval(10_000L); // 1, 2, 4, 8, 10, 10
//        return new DefaultErrorHandler(recoverer, new FixedBackOff(0, 3));
//    }


//    @Bean
//    public DeadLetterPublishingRecoverer recoverer(KafkaTemplate<?, ?> voidTemplate,
//                                                   KafkaTemplate<?, ?> bytesTemplate,
//                                                   KafkaTemplate<?, ?> stringTemplate,
//                                                   KafkaTemplate<?, ?> documentInfoTemplate) {
//        Map<Class<?>, KafkaOperations<?, ?>> templates = new LinkedHashMap<>();
//        templates.put(Void.class, voidTemplate);
//        templates.put(byte[].class, bytesTemplate);
//        templates.put(String.class, stringTemplate);
//        templates.put(DocumentInfoValue.class, documentInfoTemplate);
//        DeadLetterPublishingRecoverer deadLetterPublishingRecoverer = new DeadLetterPublishingRecoverer(templates);
//        return deadLetterPublishingRecoverer;
//    }
}

