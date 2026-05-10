package com.firstclub.membership.service.impl;

import com.firstclub.membership.entity.OrderAggregate;
import com.firstclub.membership.entity.TierCriteria;
import com.firstclub.membership.entity.User;
import com.firstclub.membership.enums.CohortType;
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
public class UserCohortEvaluator implements TierRuleEvaluator {
    private final OrderAggregateRepository orderAggregateRepository;
    private final TierCriteriaRepository tierCriteriaRepository;

    @Override
    public CriteriaType getTierType() {
        return CriteriaType.USER_COHORT;
    }

    @Override
    public TierType EvaluateRule(User user) {
        List<TierCriteria> criteriaList = tierCriteriaRepository.findByCriteriaType(CriteriaType.USER_COHORT);
        if (criteriaList.isEmpty()) {
            log.info("No criteria found for ORDER_VALUE");
            return TierType.SILVER;
        }

        TierType assignedTier = TierType.SILVER;
        for(TierCriteria criteria : criteriaList) {
            CohortType cohortType = CohortType.valueOf(String.valueOf(criteria.getAdditionalParameters().get("cohortType")));
            if(user.getCohortType().equals(cohortType)) {
                return TierType.getByDisplayName(criteria.getMembershipTier().getName());
            }
        }

        return assignedTier;
    }
}
