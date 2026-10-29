package com.mhbas.enumeration;

import java.util.Arrays;
import java.util.Optional;

public enum GoalType {
    FITNESS_TIMES("FITNESS_TIMES", "每周健身次数", "次", "WEEK", 3),
    FITNESS_MINUTES("FITNESS_MINUTES", "每周健身时长", "分钟", "WEEK", 150),
    SLEEP_HOURS("SLEEP_HOURS", "每日睡眠时长", "小时", "DAY", 7.5),
    WATER_ML("WATER_ML", "每日饮水量", "ml", "DAY", 2000),
    MEAL_COUNT("MEAL_COUNT", "每日规律三餐", "餐", "DAY", 3),
    STEPS_DAILY("STEPS_DAILY", "每日步数", "步", "DAY", 8000);

    private final String code;          // 编码
    private final String displayName;   // 显示名称
    private final String unit;          // 单位
    private final String period;        // 周期
    private final double suggestedTarget; // 建议目标值

    GoalType(String code, String displayName, String unit, String period, double suggestedTarget) {
        this.code = code;
        this.displayName = displayName;
        this.unit = unit;
        this.period = period;
        this.suggestedTarget = suggestedTarget;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUnit() {
        return unit;
    }

    public String getPeriod() {
        return period;
    }

    public double getSuggestedTarget() {
        return suggestedTarget;
    }
    // 根据编码获取枚举
    public static Optional<GoalType> fromCode(String code) {
        return Arrays.stream(values()).filter(type -> type.code.equals(code)).findFirst();
    }
}
