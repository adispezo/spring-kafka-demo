package com.example.kafkademo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class KafkaDemoConfiguration {

    @Value("${base-url:http://localhost:3000}")
    String baseURI;

    @Bean
    RestClient restClient() {
        return RestClient.builder().baseUrl(baseURI).build();
    }

}
