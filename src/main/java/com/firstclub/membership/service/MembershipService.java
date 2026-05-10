package com.firstclub.membership.service;

import com.firstclub.membership.constants.CommonConstant;
import com.firstclub.membership.dto.MembershipPlanDetailResponse;
import com.firstclub.membership.dto.MembershipPlansResponse;
import com.firstclub.membership.dto.UserMembershipDetail;
import com.firstclub.membership.entity.*;
import com.firstclub.membership.repository.*;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipPlanRepository membershipPlanRepository;
    private final TierBenefitRepository tierBenefitRepository;
    private final UserRepository userRepository;
    private final UserMembershipRepository userMembershipRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public MembershipPlanDetailResponse getMembershipPlans(String phoneNumber) {
        if (StringUtils.isBlank(phoneNumber)) {
            throw new RuntimeException("Phone number is required");
        }

        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (Objects.isNull(user)) {
            throw new RuntimeException("User not found");
        }

        List<MembershipPlan> plans = membershipPlanRepository.findByStatus(CommonConstant.ACTIVE);
        if (Objects.isNull(plans) || plans.isEmpty()) {
            throw new RuntimeException("No active membership plans found");
        }

        List<TierBenefit> benefits = tierBenefitRepository.findByMembershipTierId(user.getMembershipTier().getId());
        if (Objects.isNull(benefits) || benefits.isEmpty()) {
            throw new RuntimeException("No benefits found for user's membership tier");
        }

        List<MembershipPlansResponse> membershipPlansResponseList = plans.stream()
                .map(plan -> MembershipPlansResponse.builder()
                        .planName(plan.getPlanName())
                        .durationType(plan.getDurationType().getDisplayName())
                        .price(plan.getPrice())
                        .durationValue(plan.getDurationInDays())
                        .build())
                .toList();

        return MembershipPlanDetailResponse.builder()
                .membershipPlans(membershipPlansResponseList)
                .userTier(user.getMembershipTier().getName())
                .benefits(benefits.stream().map(benefit -> benefit.getBenefitType().getDisplayName()).toList())
                .build();
    }

    @Transactional
    public String subscribeToPlan(String phoneNumber, String planName) {
        if (StringUtils.isBlank(phoneNumber) || StringUtils.isBlank(planName)) {
            throw new RuntimeException("Phone number and plan name is required");
        }

        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (Objects.isNull(user)) {
            throw new RuntimeException("User not found");
        }

        MembershipPlan plan = membershipPlanRepository.findByPlanNameAndStatus(planName, CommonConstant.ACTIVE);
        if (Objects.isNull(plan)) {
            throw new RuntimeException("Active membership plan not found with name: " + planName);
        }

        UserMembership existingActive =
                userMembershipRepository.findByUserIdAndStatus(user.getId(), CommonConstant.ACTIVE);
        if (Objects.nonNull(existingActive)) {
            throw new RuntimeException(
                    "User already has an active subscription. Use /changePlan to upgrade or downgrade.");
        }

        ZonedDateTime startDate = ZonedDateTime.now();
        ZonedDateTime endDate = startDate.plusDays(plan.getDurationInDays());

        userMembershipRepository.save(UserMembership.builder()
                .user(user)
                .membershipPlan(plan)
                .status(CommonConstant.ACTIVE)
                .startDate(startDate)
                .endDate(endDate)
                .build());

        auditLogRepository.save(AuditLog.builder()
                .phoneNumber(user.getPhoneNumber())
                .fromMembershipPlan(null)
                .toMembershipPlan(plan)
                .build());

        return "Successfully subscribed to plan: " + planName;
    }


    @Transactional
    public String changePlan(String phoneNumber, String newPlanName) {
        if (StringUtils.isBlank(phoneNumber) || StringUtils.isBlank(newPlanName)) {
            throw new RuntimeException("Phone number and plan name are required");
        }

        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (Objects.isNull(user)) {
            throw new RuntimeException("User not found");
        }

        UserMembership existingActive =
                userMembershipRepository.findByUserIdAndStatus(user.getId(), CommonConstant.ACTIVE);
        if (Objects.isNull(existingActive)) {
            throw new RuntimeException("No active subscription to upgrade/downgrade");
        }

        if (existingActive.getMembershipPlan().getPlanName().equalsIgnoreCase(newPlanName)) {
            throw new RuntimeException("User is already on plan: " + newPlanName);
        }

        MembershipPlan newPlan =
                membershipPlanRepository.findByPlanNameAndStatus(newPlanName, CommonConstant.ACTIVE);
        if (Objects.isNull(newPlan)) {
            throw new RuntimeException("Active membership plan not found with name: " + newPlanName);
        }

        MembershipPlan previousPlan = existingActive.getMembershipPlan();

        existingActive.setStatus(CommonConstant.CANCELLED);
        existingActive.setEndDate(ZonedDateTime.now());
        userMembershipRepository.save(existingActive);

        ZonedDateTime startDate = ZonedDateTime.now();
        ZonedDateTime endDate = startDate.plusDays(newPlan.getDurationInDays());

        userMembershipRepository.save(UserMembership.builder()
                .user(user)
                .membershipPlan(newPlan)
                .status(CommonConstant.ACTIVE)
                .startDate(startDate)
                .endDate(endDate)
                .build());

        auditLogRepository.save(AuditLog.builder()
                .phoneNumber(user.getPhoneNumber())
                .fromMembershipPlan(previousPlan)
                .toMembershipPlan(newPlan)
                .build());

        boolean isUpgrade = newPlan.getDurationInDays() > previousPlan.getDurationInDays();
        return String.format("Successfully %s from %s to %s",
                isUpgrade ? "upgraded" : "downgraded",
                previousPlan.getPlanName(), newPlan.getPlanName());
    }

    @Transactional
    public String cancelSubscription(String phoneNumber) {
        if (StringUtils.isBlank(phoneNumber)) {
            throw new RuntimeException("Phone number is required");
        }

        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (Objects.isNull(user)) {
            throw new RuntimeException("User not found");
        }

        UserMembership existingMembership =
                userMembershipRepository.findByUserIdAndStatus(user.getId(), CommonConstant.ACTIVE);
        if (Objects.isNull(existingMembership)) {
            throw new RuntimeException("No active subscription found for user");
        }

        existingMembership.setStatus(CommonConstant.CANCELLED);
        userMembershipRepository.save(existingMembership);

        /// TODO - We can also add logic to calculate the refund amount based on the remaining days of the subscription and the price of the plan

        auditLogRepository.save(AuditLog.builder()
                .phoneNumber(user.getPhoneNumber())
                .fromMembershipPlan(existingMembership.getMembershipPlan())
                .toMembershipPlan(null)
                .build());

        return "Successfully cancelled subscription for user with phone number: " + phoneNumber;
    }

    @Transactional(readOnly = true)
    public UserMembershipDetail getUserMembershipDetails(String phoneNumber) {
        if (StringUtils.isBlank(phoneNumber)) {
            throw new RuntimeException("Phone number is required");
        }

        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (Objects.isNull(user)) {
            throw new RuntimeException("User not found");
        }

        UserMembership existingMembership =
                userMembershipRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), CommonConstant.ACTIVE);
        if (Objects.isNull(existingMembership)) {
            throw new RuntimeException("No subscription found for user");
        }

        return UserMembershipDetail.builder()
                .planName(existingMembership.getMembershipPlan().getPlanName())
                .startDate(existingMembership.getStartDate().toString())
                .endDate(existingMembership.getEndDate().toString())
                .tier(Objects.nonNull(user.getMembershipTier()) ? user.getMembershipTier().getName() : null)
                .build();
    }
}
