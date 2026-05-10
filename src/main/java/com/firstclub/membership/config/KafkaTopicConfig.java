package com.firstclub.membership.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class KafkaTopicConfig {

    @Value("${app.kafka.topics.payment}")
    private String paymentTopic;

    @Value("${app.kafka.topics.order}")
    private String orderTopic;

}
