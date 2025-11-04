package com.qkdt.demo_kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

public class KafkaTopicConf {
    @Value("${app.kafka.topic-name}")
    private String myTopic;

    @Bean
    public NewTopic myTopic() {
        // Tạo một Topic mới tên là "my-topic"
        return TopicBuilder.name(myTopic)
                .partitions(3) // Có thể cấu hình số partition
                .replicas(1)  // Số bản sao
                .build();
    }
}
