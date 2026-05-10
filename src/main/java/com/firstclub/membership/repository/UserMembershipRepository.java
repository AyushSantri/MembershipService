package com.firstclub.membership.repository;

import com.firstclub.membership.entity.UserMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {

    UserMembership findByUserIdAndStatus(Long userId, String status);

    UserMembership findByUserId(Long userId);

    UserMembership findFirstByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
}

