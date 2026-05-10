package com.firstclub.membership.consumers;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer {

    @KafkaListener(
            topics = "${app.kafka.topics.payment}",
            groupId = "payment-group"
    )
    public void consumePayment(String message) {
        System.out.println("Payment Event Received: " + message);

        // Created this to track the membership payment events,
        // but currently we are not doing anything with the payment events.

        //I did create a payment table to track the payment events
    }
}
