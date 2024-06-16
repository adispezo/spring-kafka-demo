package com.example.kafkademo.producer;

import com.example.kafkademo.DocumentGeneratedKey;
import com.example.kafkademo.DocumentGeneratedValue;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
@AllArgsConstructor
public class DocumentGeneratedProducer {

    private final KafkaTemplate<DocumentGeneratedKey, DocumentGeneratedValue> kafkaTemplate;


    public void produceSuccessful(String referenceId, String orderId, String documentKey) {
        DocumentGeneratedValue documentGeneratedValue = new DocumentGeneratedValue(true, orderId, documentKey, null);
        produce(referenceId, documentGeneratedValue);
    }

    public void produceError(String referenceId, String orderId, String errorMessage) {
        DocumentGeneratedValue documentGeneratedValue = new DocumentGeneratedValue(false, orderId, null, errorMessage);
        produce(referenceId, documentGeneratedValue);
    }

    private void produce(String referenceId, DocumentGeneratedValue documentGeneratedValue){
        DocumentGeneratedKey documentGeneratedKey = new DocumentGeneratedKey(referenceId);

//        CompletableFuture<SendResult<DocumentGeneratedKey, DocumentGeneratedValue>> future = this.kafkaTemplate.send("document-generated", documentGeneratedKey, documentGeneratedValue);
//        future.whenComplete((result, ex) -> {
//            if(ex == null){
//                log.info("Successfully send to {} topic, on partition {} and offset {} in {} milliseconds",
//                        "document-generated",
//                        result.getRecordMetadata().partition(),
//                        result.getRecordMetadata().offset(),
//                        System.currentTimeMillis() - result.getRecordMetadata().timestamp());
//            } else {
//                log.error("Handle failure");
//            }
//        });

        try {
            SendResult<DocumentGeneratedKey, DocumentGeneratedValue> result =
                    this.kafkaTemplate.send("document-generated", documentGeneratedKey, documentGeneratedValue).get(10, TimeUnit.SECONDS);
            log.info("Successfully send to {} topic, on partition {} and offset {} in {} milliseconds",
                    "document-generated",
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    System.currentTimeMillis() - result.getRecordMetadata().timestamp());
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Handle failure");
        }
    }
}
