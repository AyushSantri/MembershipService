package com.firstclub.membership.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CohortType {
    GOOD("Good"),
    BAD("Bad"),
    Medium("Medium");

    private final String displayName;
}
