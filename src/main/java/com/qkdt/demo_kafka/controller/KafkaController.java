package com.qkdt.demo_kafka.controller;

import com.qkdt.demo_kafka.service.MessageProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KafkaController {

    private final MessageProducerService messageProducerService;

    @PostMapping("/send")
    public String sendMessage(@RequestParam("message") String message) {
        messageProducerService.sendMessage(message);

        return "Message sent to Kafka topic 'my-topic': " + message;
    }

}
