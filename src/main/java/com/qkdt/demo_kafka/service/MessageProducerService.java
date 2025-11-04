package com.qkdt.demo_kafka.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageProducerService {

    private static final Logger logger = LoggerFactory.getLogger(MessageProducerService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topic-name}")
    private String topicName;

    public void sendMessage(String message) {
        kafkaTemplate.send(topicName, message);
        logger.info("Sent message: {}", message);
    }
}
