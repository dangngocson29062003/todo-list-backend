package com.example.weaver.enums;

import lombok.Getter;

@Getter
public enum Priority {
    LOW(1),
    NORMAL(2),
    HIGH(3),
    URGENT(4);

    private final int rank;

    Priority(int rank) {
        this.rank = rank;
    }

}
