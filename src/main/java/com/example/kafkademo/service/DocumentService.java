package com.example.kafkademo.service;


import com.example.kafkademo.Product;
import com.example.kafkademo.domain.DocumentRequest;
import com.example.kafkademo.domain.DocumentResponse;
import com.example.kafkademo.producer.DocumentGeneratedProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentService {

    private final RestClient restClient;
    private final DocumentGeneratedProducer documentGeneratedProducer;

    public void createDocument(String referenceId, Product product, String orderId) {

        // get order and relation info from db

        // compose document structure
        DocumentRequest documentRequest = null;
        switch (product){
            case CAR -> {
                log.info("compose car document structure");
                documentRequest = new DocumentRequest(product.name());
            }
            case HOUSE -> {
                log.info("compose house document structure");
                documentRequest = new DocumentRequest(product.name());
            }
        }

        // call post documents endpoint
        final DocumentResponse documentResponse = Objects.requireNonNull(restClient
                        .post()
                        .uri("/documents/" + orderId)
                        .body(documentRequest)
                        .retrieve()
                        .body(DocumentResponse.class));

        documentGeneratedProducer.produceSuccessful(referenceId, orderId, documentResponse.documentKey());
    }
}
