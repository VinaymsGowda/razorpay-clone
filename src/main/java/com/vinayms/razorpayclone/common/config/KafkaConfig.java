package com.vinayms.razorpayclone.common.config;

import com.vinayms.razorpayclone.common.enums.EventAggregateType;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Arrays;

/**
 * Declares the topics explicitly so they exist with the configured partitions/replicas
 * instead of relying on broker auto-creation.
 */
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaTopicProperties topicProperties;

    @Bean
    public KafkaAdmin.NewTopics outboxTopics() {
        NewTopic[] topics = Arrays.stream(EventAggregateType.values())
                .map(topicProperties::getTopic)
                .distinct()
                .map(name -> TopicBuilder.name(name)
                        .partitions(topicProperties.getPartitions())
                        .replicas(topicProperties.getReplicas())
                        .build())
                .toArray(NewTopic[]::new);
        return new KafkaAdmin.NewTopics(topics);
    }
}
