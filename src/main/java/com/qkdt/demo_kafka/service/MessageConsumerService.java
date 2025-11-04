package com.qkdt.demo_kafka.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MessageConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumerService.class);

    @KafkaListener(topics = "app.kafka.topic-name", groupId = "my-group")
    public void listen(String message) {
        logger.info("Received message: {}", message);
    }
}
