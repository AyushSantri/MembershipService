package com.firstclub.membership.service;

import com.firstclub.membership.dto.CreateUserDto;
import com.firstclub.membership.entity.User;
import com.firstclub.membership.enums.CohortType;
import com.firstclub.membership.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TierRuleEvaluatorService tierRuleEvaluatorService;

    public String createUser(CreateUserDto createUserDto) {
        userRepository.save(
                User.builder()
                        .name(createUserDto.getName())
                        .phoneNumber(createUserDto.getPhoneNumber())
                        .cohortType(CohortType.GOOD)
                        .build()
        );

        tierRuleEvaluatorService.evaluateRules(createUserDto.getPhoneNumber());
        return "User created successfully: " + createUserDto.getName() + " (" + createUserDto.getPhoneNumber() + ")";
    }
}
