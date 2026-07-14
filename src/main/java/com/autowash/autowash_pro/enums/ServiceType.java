package com.autowash.autowash_pro.enums;

public enum ServiceType {
    BASIC, PREMIUM, FULL_DETAIL;

    public int getBasePrice() {
        return switch (this) {
            case BASIC       -> 30000;
            case PREMIUM     -> 50000;
            case FULL_DETAIL -> 80000;
        };
    }
}