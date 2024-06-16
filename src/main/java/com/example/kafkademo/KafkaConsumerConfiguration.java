package com.example.kafkademo;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;

import java.util.HashMap;
import java.util.Map;

//@Configuration
@Slf4j
public class KafkaConsumerConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:kafkatest}")
    private String groupId;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.ErrorHandlingDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.ErrorHandlingDeserializer");
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("spring.deserializer.key.delegate.class", "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        props.put("spring.deserializer.value.delegate.class", "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        props.put("specific.avro.reader", true);
        return props;
    }

    @Bean
    public KafkaMessageListenerContainer<String, Object> container() {
        ContainerProperties containerProps = new ContainerProperties("documentInfo");
        containerProps.setMessageListener(messageListener());
        return new KafkaMessageListenerContainer<>(consumerFactory(), containerProps);
    }

    @Bean
    public MessageListener<DocumentInfoKey, DocumentInfoValue> messageListener() {
        return record -> log.info("Received Message: " + record.value());
    }
}
