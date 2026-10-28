package com.vinayms.razorpayclone.common.config;

import com.vinayms.razorpayclone.common.enums.EventAggregateType;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Binds the top-level {@code kafka} block of application.yaml.
 * Named differently from Spring Boot's own KafkaProperties to avoid confusion.
 */
@Component
@ConfigurationProperties(prefix = "kafka")
@Getter
@Setter
public class KafkaTopicProperties {

    private Map<String, String> topics = new HashMap<>();
    private int partitions = 3;
    private int replicas = 1;

    /** Fail fast at startup if an aggregate type has no topic configured. */
    @PostConstruct
    void validate() {
        for (EventAggregateType type : EventAggregateType.values()) {
            getTopic(type);
        }
    }

    public String getTopic(EventAggregateType aggregateType) {
        String topic = topics.get(aggregateType.name().toLowerCase(Locale.ROOT));
        if (topic == null || topic.isBlank()) {
            throw new IllegalStateException("No Kafka topic configured for aggregate type: " + aggregateType
                    + " (expected kafka.topics." + aggregateType.name().toLowerCase(Locale.ROOT) + ")");
        }
        return topic;
    }
}
