package com.firstclub.membership.controller;

import com.firstclub.membership.dto.OrderDetail;
import com.firstclub.membership.service.KafkaProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/kafka")
@RequiredArgsConstructor
@Tag(name = "Kafka Events",
        description = "Test endpoints for publishing payment and order events. "
                + "In production these would be triggered by upstream services.")
public class KafkaEventController {
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

//    @PostMapping("/sendPaymentEvent")
//    @Operation(summary = "Publish a payment event to the payment-topic")
//    public String sendPaymentEvent(
//            @Parameter(description = "Free-form payment event payload (string)")
//            @RequestParam("message") String message) {
//        kafkaProducerService.sendPaymentEvent(message);
//        return "Payment event sent: " + message;
//    }



    // this is to simulate an order event that would be published by an upstream order service. In production, this would not be a REST endpoint but rather triggered by the order service when an order is placed.
    @PostMapping("/sendOrderEvent")
    @Operation(
            summary = "Publish an order event",
            description = "Order events drive monthly aggregation and tier re-evaluation "
                    + "via OrderConsumer."
    )
    public String sendOrderEvent(@RequestBody OrderDetail orderDetail) {
        String message = objectMapper.writeValueAsString(orderDetail);
        kafkaProducerService.sendOrderEvent(message);
        return "Order event sent: " + message;
    }
}
