package com.autowash.autowash_pro.enums;

public enum Tier {
    MEMBER, SILVER, GOLD, PLATINUM;

    public int getBookingWindowDays() {
        return switch (this) {
            case MEMBER -> 7;
            case SILVER -> 10;
            case GOLD -> 12;
            case PLATINUM -> 14;
        };
    }

    public int getPriorityScore() {
        return switch (this) {
            case MEMBER -> 10;
            case SILVER -> 20;
            case GOLD -> 30;
            case PLATINUM -> 40;
        };
    }

    public double getPointMultiplier() {
        return switch (this) {
            case MEMBER -> 1.0;
            case SILVER -> 1.05;
            case GOLD -> 1.10;
            case PLATINUM -> 1.15;
        };
    }

    // 🌟 THÊM LOGIC: Tự động tính toán Hạng dựa trên tổng số điểm tích lũy lũy kế
    public static Tier getNextTierFromPoints(int points) {
        if (points >= 1000)
            return PLATINUM; // Từ 1000 điểm lên PLATINUM
        if (points >= 300)
            return GOLD; // Từ 300 điểm lên GOLD
        if (points >= 100)
            return SILVER; // Từ 100 điểm lên SILVER
        return MEMBER; // Dưới 100 điểm giữ hạng MEMBER
    }
}