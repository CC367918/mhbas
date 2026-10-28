package com.mhbas.constant;

import java.util.List;

public final class HealthConstant {
    public static final List<String> FITNESS_TYPES = List.of(
            "力量训练", "跑步", "瑜伽", "骑行", "球类", "游泳", "其他"
    );

    public static final List<String> FITNESS_INTENSITIES = List.of("低强度", "中等强度", "高强度");
    public static final List<String> MEAL_TYPES = List.of("早餐", "午餐", "晚餐");
    public static final List<String> SLEEP_STATUSES = List.of("精力充沛", "一般", "疲惫");

    private HealthConstant() {
    }
}
