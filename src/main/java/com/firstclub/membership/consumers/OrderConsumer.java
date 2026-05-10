package com.firstclub.membership.consumers;

import com.firstclub.membership.dto.OrderDetail;
import com.firstclub.membership.service.OrderService;
import com.firstclub.membership.service.TierRuleEvaluatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Log4j2
public class OrderConsumer {
    private final ObjectMapper objectMapper;
    private final OrderService orderService;
    private final TierRuleEvaluatorService tierRuleEvaluatorService;

    @KafkaListener(
            topics = "${app.kafka.topics.order}",
            groupId = "order-group"
    )
    public void consumeOrder(String message) {
        log.info("Order Event Received: {}", message);
        OrderDetail orderDetail = objectMapper.readValue(message, OrderDetail.class);
        orderService.processOrder(orderDetail);
        tierRuleEvaluatorService.evaluateRules(orderDetail.getPhoneNumber());
    }
}
