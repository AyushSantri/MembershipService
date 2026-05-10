package com.firstclub.membership.controller;

import com.firstclub.membership.dto.MembershipPlanDetailResponse;
import com.firstclub.membership.service.MembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor
@Tag(name = "Membership", description = "Plans, subscriptions, tier transitions, and member details")
public class MembershipController {
    private final MembershipService membershipService;

    @GetMapping("/plans")
    @Operation(
            summary = "List active plans for a user",
            description = "Returns every active membership plan along with the user's "
                    + "current tier and the benefits granted by that tier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plans returned"),
            @ApiResponse(responseCode = "500", description = "User missing, no active plans, or no benefits configured")
    })
    public ResponseEntity<Object> getMembershipPlans(
            @RequestParam("phone_number") String phoneNumber) {
        try {
            MembershipPlanDetailResponse response = membershipService.getMembershipPlans(phoneNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving membership plans: " + e.getMessage());
        }
    }

    @PostMapping("/subscribe")
    @Operation(
            summary = "Subscribe a user to a plan",
            description = "Creates a new active subscription. Fails if the user already "
                    + "has an active subscription — use /changePlan for upgrade/downgrade."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription created"),
            @ApiResponse(responseCode = "500", description = "User missing, plan missing, or user already has an active subscription")
    })
    public ResponseEntity<String> subscribeToMembership(
            @RequestParam("phone_number") String phoneNumber,
            @RequestParam("plan_name") String planName) {
        try {
            return ResponseEntity.ok(membershipService.subscribeToPlan(phoneNumber, planName));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error subscribing to membership: " + e.getMessage());
        }
    }

    @PatchMapping("/changePlan")
    @Operation(
            summary = "Upgrade or downgrade an active subscription",
            description = "Closes the user's current active subscription and opens a new "
                    + "one for the requested plan in a single transaction. Direction "
                    + "(upgrade vs downgrade) is inferred from plan duration."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plan changed"),
            @ApiResponse(responseCode = "500", description = "User missing, target plan missing, or user already on the requested plan")
    })
    public ResponseEntity<String> changePlan(
            @RequestParam("phone_number") String phoneNumber,
            @RequestParam("new_plan_name") String newPlanName) {
        try {
            return ResponseEntity.ok(membershipService.changePlan(phoneNumber, newPlanName));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error changing plan: " + e.getMessage());
        }
    }

    @PatchMapping("/unsubscribe")
    @Operation(
            summary = "Cancel an active subscription",
            description = "Marks the user's active subscription as CANCELLED. No prorated "
                    + "refund is applied (TODO)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription cancelled"),
            @ApiResponse(responseCode = "500", description = "User missing or no active subscription")
    })
    public ResponseEntity<String> unsubscribeFromMembership(
            @RequestParam("phone_number") String phoneNumber) {
        try {
            return ResponseEntity.ok(membershipService.cancelSubscription(phoneNumber));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error unsubscribing from membership: " + e.getMessage());
        }
    }

    @GetMapping("/userMembershipDetails")
    @Operation(
            summary = "Get the user's most recent subscription",
            description = "Returns plan name, start/end dates, and the user's current tier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membership details returned"),
            @ApiResponse(responseCode = "500", description = "User missing or no subscription found")
    })
    public ResponseEntity<Object> getUserMembershipDetails(
            @RequestParam("phone_number") String phoneNumber) {
        try {
            return ResponseEntity.ok(membershipService.getUserMembershipDetails(phoneNumber));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving user membership details: " + e.getMessage());
        }
    }
}
