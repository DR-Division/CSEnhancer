package com.division.enums;

public enum Priority {

    LOW(1),NORMAL(2),HIGH(3);

    private int value;

    Priority(int value) {
        this.value = value;
    }


    public int getValue() {
        return this.value;
    }
}
