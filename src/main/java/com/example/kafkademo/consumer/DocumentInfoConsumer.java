package com.example.kafkademo.consumer;

import com.example.kafkademo.DocumentInfoKey;
import com.example.kafkademo.DocumentInfoValue;
import com.example.kafkademo.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentInfoConsumer {

    private final DocumentService documentService;

//    @RetryableTopic(
//            attempts = "5",
//            backoff = @Backoff(
//                    delay = 1000,
//                    multiplier = 2,
//                    maxDelay = 3000
//            ),
//            retryTopicSuffix = "-darkside",
//            dltTopicSuffix = "-darkside.DLT"
//    )
//    @KafkaListener(topicPartitions =
//        { @TopicPartition(topic = "documentInfo", partitions = { "0", "1" }),
//                @TopicPartition(topic = "documentInfo", partitions = "0",
//                        partitionOffsets = @PartitionOffset(partition = "*", initialOffset = "10"))
//        })
    @KafkaListener(topics = "documentInfo")
    public void consume(final ConsumerRecord<DocumentInfoKey, DocumentInfoValue> documentInfo,
                        @Header(name="customExceptionHeader", required = false) String customExceptionHeader){
        log.info("customExceptionHeader: " + customExceptionHeader);
        documentService.createDocument(documentInfo.key().getReferenceId().toString(),
                documentInfo.value().getProduct(), documentInfo.value().getOrderId().toString());
    }
}
