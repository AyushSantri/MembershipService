package com.firstclub.membership.service.impl;

import com.firstclub.membership.entity.OrderAggregate;
import com.firstclub.membership.entity.TierCriteria;
import com.firstclub.membership.entity.User;
import com.firstclub.membership.enums.CriteriaType;
import com.firstclub.membership.enums.TierType;
import com.firstclub.membership.repository.OrderAggregateRepository;
import com.firstclub.membership.repository.TierCriteriaRepository;
import com.firstclub.membership.service.RuleEvaluator.TierRuleEvaluator;
import com.firstclub.membership.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Log4j2
public class OrderValueEvaluator implements TierRuleEvaluator {
    private final OrderAggregateRepository orderAggregateRepository;
    private final TierCriteriaRepository tierCriteriaRepository;

    @Override
    public CriteriaType getTierType() {
        return CriteriaType.ORDER_VALUE;
    }

    @Override
    public TierType EvaluateRule(User user) {
        List<TierCriteria> criteriaList = tierCriteriaRepository.findByCriteriaType(CriteriaType.ORDER_VALUE);
        if (criteriaList.isEmpty()) {
            log.info("No criteria found for ORDER_VALUE");
            return TierType.SILVER;
        }

        String orderMonthAndYear = DateTimeUtil.getCurrentYearAndMonth();
        OrderAggregate orderAggregate = orderAggregateRepository.findByOrderMonthYear(orderMonthAndYear);
        if (Objects.isNull(orderAggregate)) {
            log.info("No order aggregate found for month and year: {}", orderMonthAndYear);
            return TierType.SILVER;
        }

        int priority = 0;
        TierType assignedTier = TierType.SILVER;
        for(TierCriteria criteria : criteriaList) {
            if (orderAggregate.getTotalAmount().doubleValue() >= criteria.getThresholdValue()) {
                log.info("User {} meets criteria for tier: {}", user.getId(), criteria.getMembershipTier().getName());
                if (criteria.getPriority() > priority) {
                    priority = criteria.getPriority();
                    assignedTier = TierType.getByDisplayName(criteria.getMembershipTier().getName());
                }
            }
        }

        return assignedTier;
    }
}
