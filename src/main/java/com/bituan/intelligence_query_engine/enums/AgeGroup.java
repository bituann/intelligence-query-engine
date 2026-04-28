package com.bituan.intelligence_query_engine.enums;

public enum AgeGroup {
    child, teenager, adult, senior;

    public static AgeGroup resolve (int age) {
        if (age >= 60) return senior;
        if (age >= 20) return adult;
        if (age >= 13) return teenager;
        return child;
    };
}
