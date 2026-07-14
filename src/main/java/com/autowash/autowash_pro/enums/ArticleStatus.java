package com.autowash.autowash_pro.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ArticleStatus {
    PUBLISHED,
    DRAFT;

    @JsonCreator
    public static ArticleStatus fromString(String value) {
        if (value == null) return null;
        return ArticleStatus.valueOf(value.toUpperCase().trim());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}