package com.qkdt.demo_kafka.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import javax.print.attribute.standard.Media;

@Configuration
public class WebClientConf {
    
    @Value("${python.be.url}")
    private String pythonUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(pythonUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
