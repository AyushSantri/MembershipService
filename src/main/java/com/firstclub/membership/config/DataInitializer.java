package com.firstclub.membership.config;

import com.firstclub.membership.constants.CommonConstant;
import com.firstclub.membership.entity.MembershipPlan;
import com.firstclub.membership.entity.MembershipTier;
import com.firstclub.membership.entity.TierBenefit;
import com.firstclub.membership.entity.TierCriteria;
import com.firstclub.membership.enums.BenefitType;
import com.firstclub.membership.enums.CriteriaType;
import com.firstclub.membership.enums.DurationType;
import com.firstclub.membership.enums.Operators;
import com.firstclub.membership.repository.MembershipPlanRepository;
import com.firstclub.membership.repository.MembershipTierRepository;
import com.firstclub.membership.repository.TierBenefitRepository;
import com.firstclub.membership.repository.TierCriteriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
@Log4j2
public class DataInitializer implements ApplicationRunner {

    private final MembershipTierRepository membershipTierRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final TierBenefitRepository tierBenefitRepository;
    private final TierCriteriaRepository tierCriteriaRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedTiers();
        seedPlans();
        seedBenefits();
        seedCriteria();
    }

    private void seedTiers() {
        if (membershipTierRepository.count() > 0) {
            log.info("MembershipTier table already populated, skipping seed");
            return;
        }

        MembershipTier silver = new MembershipTier();
        silver.setName("Silver");
        silver.setLevel(1);
        silver.setDescription("Entry tier — default on signup");
        silver.setActive(true);

        MembershipTier gold = new MembershipTier();
        gold.setName("Gold");
        gold.setLevel(2);
        gold.setDescription("Earned via spend or count");
        gold.setActive(true);

        MembershipTier platinum = new MembershipTier();
        platinum.setName("Platinum");
        platinum.setLevel(3);
        platinum.setDescription("Top tier — highest perks");
        platinum.setActive(true);

        membershipTierRepository.saveAll(List.of(silver, gold, platinum));
        log.info("Seeded membership tiers: Silver, Gold, Platinum");
    }

    private void seedPlans() {
        if (membershipPlanRepository.count() > 0) {
            log.info("MembershipPlan table already populated, skipping seed");
            return;
        }

        MembershipPlan monthly = new MembershipPlan();
        monthly.setPlanName("Monthly");
        monthly.setDurationType(DurationType.MONTHLY);
        monthly.setDurationInDays(30);
        monthly.setPrice(new BigDecimal("199.00"));
        monthly.setStatus(CommonConstant.ACTIVE);

        MembershipPlan quarterly = new MembershipPlan();
        quarterly.setPlanName("Quarterly");
        quarterly.setDurationType(DurationType.QUARTERLY);
        quarterly.setDurationInDays(90);
        quarterly.setPrice(new BigDecimal("499.00"));
        quarterly.setStatus(CommonConstant.ACTIVE);

        MembershipPlan yearly = new MembershipPlan();
        yearly.setPlanName("Yearly");
        yearly.setDurationType(DurationType.ANNUALLY);
        yearly.setDurationInDays(365);
        yearly.setPrice(new BigDecimal("1499.00"));
        yearly.setStatus(CommonConstant.ACTIVE);

        membershipPlanRepository.saveAll(List.of(monthly, quarterly, yearly));
        log.info("Seeded membership plans: Monthly (199), Quarterly (499), Yearly (1499)");
    }

    private void seedBenefits() {
        MembershipTier silver = membershipTierRepository.findByName("Silver");
        MembershipTier gold = membershipTierRepository.findByName("Gold");
        MembershipTier platinum = membershipTierRepository.findByName("Platinum");

        if (silver != null && tierBenefitRepository.findByMembershipTierId(silver.getId()).isEmpty()) {
            tierBenefitRepository.saveAll(List.of(
                    benefit(silver, BenefitType.FREE_DELIVERY,
                            Map.of("min_order_value", 999)),
                    benefit(silver, BenefitType.DISCOUNTS,
                            Map.of("percent", 5, "categories", List.of("fashion")))
            ));
            log.info("Seeded Silver-tier benefits");
        }

        if (gold != null && tierBenefitRepository.findByMembershipTierId(gold.getId()).isEmpty()) {
            tierBenefitRepository.saveAll(List.of(
                    benefit(gold, BenefitType.FREE_DELIVERY,
                            Map.of("min_order_value", 299)),
                    benefit(gold, BenefitType.DISCOUNTS,
                            Map.of("percent", 10,
                                    "categories", List.of("fashion", "home"))),
                    benefit(gold, BenefitType.EARLY_ACCESS,
                            Map.of("hours_before_sale", 24))
            ));
            log.info("Seeded Gold-tier benefits");
        }

        if (platinum != null && tierBenefitRepository.findByMembershipTierId(platinum.getId()).isEmpty()) {
            tierBenefitRepository.saveAll(List.of(
                    benefit(platinum, BenefitType.FREE_DELIVERY,
                            Map.of("min_order_value", 0)),
                    benefit(platinum, BenefitType.DISCOUNTS,
                            Map.of("percent", 20,
                                    "categories", List.of("fashion", "home", "electronics", "grocery"))),
                    benefit(platinum, BenefitType.EARLY_ACCESS,
                            Map.of("hours_before_sale", 48))
            ));
            log.info("Seeded Platinum-tier benefits");
        }
    }

    private void seedCriteria() {
        MembershipTier gold = membershipTierRepository.findByName("Gold");
        MembershipTier platinum = membershipTierRepository.findByName("Platinum");


        if (gold != null && tierCriteriaRepository.findByCriteriaType(CriteriaType.ORDER_COUNT).stream()
                .noneMatch(c -> c.getMembershipTier().getId().equals(gold.getId()))) {
            tierCriteriaRepository.saveAll(List.of(
                    criterion(gold, CriteriaType.ORDER_COUNT, 10, 30,
                            Operators.GREATER_THAN_OR_EQUALS, Map.of(), 1),
                    criterion(gold, CriteriaType.ORDER_VALUE, 10000.00, 30,
                            Operators.GREATER_THAN_OR_EQUALS, Map.of(), 2),
                    criterion(gold, CriteriaType.USER_COHORT, 0, 0,
                            Operators.IN, Map.of("cohortType", "GOOD"), 3)
            ));
            log.info("Seeded Gold-tier criteria");
        }


        if (platinum != null && tierCriteriaRepository.findByCriteriaType(CriteriaType.ORDER_COUNT).stream()
                .noneMatch(c -> c.getMembershipTier().getId().equals(platinum.getId()))) {
            tierCriteriaRepository.saveAll(List.of(
                    criterion(platinum, CriteriaType.ORDER_COUNT, 30, 30,
                            Operators.GREATER_THAN_OR_EQUALS, Map.of(), 10),
                    criterion(platinum, CriteriaType.ORDER_VALUE, 50000.00, 30,
                            Operators.GREATER_THAN_OR_EQUALS, Map.of(), 11)
            ));
            log.info("Seeded Platinum-tier criteria");
        }
    }


    private TierBenefit benefit(MembershipTier tier, BenefitType type, Map<String, Object> config) {
        TierBenefit b = new TierBenefit();
        b.setMembershipTier(tier);
        b.setBenefitType(type);
        b.setConfig(config);
        b.setActive(true);
        return b;
    }

    private TierCriteria criterion(MembershipTier tier, CriteriaType type,
                                   double threshold, int periodDays,
                                   Operators op, Map<String, Object> extra, int priority) {
        TierCriteria c = new TierCriteria();
        c.setMembershipTier(tier);
        c.setCriteriaType(type);
        c.setThresholdValue(threshold);
        c.setPeriodInDays(periodDays);
        c.setOperator(op);
        c.setAdditionalParameters(extra);
        c.setPriority(priority);
        return c;
    }
}
