package com.example.kafkademo.integration;


import com.example.kafkademo.DocumentGeneratedKey;
import com.example.kafkademo.DocumentGeneratedValue;
import com.example.kafkademo.DocumentInfoKey;
import com.example.kafkademo.DocumentInfoValue;
import com.example.kafkademo.Product;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaString;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.TopicPartitionOffset;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integration-tests")
@EmbeddedKafka(controlledShutdown = true, topics = {"documentInfo", "document-generated"})
@Slf4j
@WireMockTest(httpPort = 9999)
@DirtiesContext
public class DocumentGeneratorTest {

    protected final EmbeddedKafkaBroker embeddedKafkaBroker;
    protected final KafkaProperties kafkaProperties;

    protected KafkaTemplate<DocumentInfoKey, DocumentInfoValue> kafkaTemplate;
    protected BlockingQueue<ConsumerRecord<DocumentGeneratedKey, DocumentGeneratedValue>> documentInfos = new LinkedBlockingQueue<>();

    private KafkaMessageListenerContainer<DocumentGeneratedKey, DocumentGeneratedValue> documentGeneratedContainer;

    @Autowired
    public DocumentGeneratorTest(final EmbeddedKafkaBroker embeddedKafkaBroker, final KafkaProperties kafkaProperties) {
        this.embeddedKafkaBroker = embeddedKafkaBroker;
        this.kafkaProperties = kafkaProperties;
        this.kafkaTemplate = getKafkaTemplate();
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        registerSchemas();
    }

    @BeforeEach
    public void setup() throws Exception {
        registerSchemas();
        this.documentGeneratedContainer = this.setupListenerContainer("document-generated");
    }

    @AfterEach
    public void cleanup() {
        this.documentGeneratedContainer.stop(true);
    }

    @Test
    void testSuccessfulEvent() throws InterruptedException, ExecutionException {
        final String referenceId = "22222";
        final String orderId = "200";
        final Product product = Product.HOUSE;

        final DocumentInfoKey documentInfoKey = new DocumentInfoKey(referenceId);
        final DocumentInfoValue documentInfoValue = new DocumentInfoValue(orderId, product);

        this.kafkaTemplate.send("documentInfo", 0, documentInfoKey, documentInfoValue).get();

        final ConsumerRecord<DocumentGeneratedKey, DocumentGeneratedValue> documentGenerated = this.documentInfos.poll(30, TimeUnit.SECONDS);

        assertThat(documentGenerated).isNotNull();
        assertThat(documentGenerated.value()).isNotNull();
        final DocumentGeneratedValue documentGeneratedValue = documentGenerated.value();
        assertThat(documentGeneratedValue.getSuccessful()).isTrue();
        assertThat(documentGeneratedValue.getOrderId().toString()).isEqualTo(orderId);
        assertThat(documentGeneratedValue.getDocumentKey().toString()).isEqualTo("123456789");
    }

    static void registerSchemas() throws Exception {
        registerSchema(1, "documentInfo-key", DocumentInfoKey.getClassSchema().toString());
        registerSchema(2, "documentInfo-value", DocumentInfoValue.getClassSchema().toString());
        registerSchema(3, "document-generated-key", DocumentGeneratedKey.getClassSchema().toString());
        registerSchema(4, "document-generated-value", DocumentGeneratedValue.getClassSchema().toString());

    }

    static void registerSchema(final int schemaId, final String topic, final String schema) throws Exception {
        final SchemaString schemaString = new SchemaString(schema);
        WireMock.stubFor((post(urlPathMatching("/subjects/" + topic))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"id\":" + schemaId + "}"))));

        WireMock.stubFor(get(urlPathMatching("/schemas/ids/" + schemaId))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(schemaString.toJson())));
    }

    protected KafkaMessageListenerContainer<DocumentGeneratedKey, DocumentGeneratedValue> setupListenerContainer(final String topic) {
        final KafkaMessageListenerContainer<DocumentGeneratedKey, DocumentGeneratedValue> container = getKafkaMessageListenerContainerDocumentGenerated(topic);
        container.setupMessageListener((MessageListener<DocumentGeneratedKey, DocumentGeneratedValue>) record -> {
            System.out.println("Consuming from topic: " + topic);
            this.documentInfos.add(record);
        });
        container.start();

        ContainerTestUtils.waitForAssignment(container, this.embeddedKafkaBroker.getPartitionsPerTopic());
        return container;
    }

    private KafkaMessageListenerContainer<DocumentGeneratedKey, DocumentGeneratedValue> getKafkaMessageListenerContainerDocumentGenerated(final String topic) {
        final Map<String, Object> consumerProps = this.kafkaProperties.buildConsumerProperties(null);
        consumerProps.put("specific.avro.reader", true);
        consumerProps.put("group.id", "integration-test");

        final ContainerProperties containerProperties = new ContainerProperties(
                new TopicPartitionOffset(topic, 0),
                new TopicPartitionOffset(topic, 1));

        final ConcurrentKafkaListenerContainerFactory<DocumentGeneratedKey, DocumentGeneratedValue> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerProps));
        factory.setMissingTopicsFatal(false);
        return new KafkaMessageListenerContainer<>(factory.getConsumerFactory(), containerProperties);
    }

    protected KafkaTemplate<DocumentInfoKey, DocumentInfoValue> getKafkaTemplate() {
        final Map<String, Object> senderProps = this.kafkaProperties.buildProducerProperties(null);
        final ProducerFactory<DocumentInfoKey, DocumentInfoValue> producerFactory = new DefaultKafkaProducerFactory<>(senderProps);
        return new KafkaTemplate<>(producerFactory);
    }

}
