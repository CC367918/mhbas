package com.mhbas.utils;

import java.util.Map;

/**
 * 使用 MET 公式估算健身热量。
 * 由于当前用户表不保存体重，采用 65kg 作为统一演示基准。
 */
public final class FitnessCalorieCalculator {
    private static final double DEFAULT_WEIGHT_KG = 65.0;
    private static final Map<String, Double> MET_BY_TYPE = Map.of(
            "力量训练", 5.0,
            "跑步", 8.0,
            "瑜伽", 2.5,
            "骑行", 6.0,
            "球类", 7.0,
            "游泳", 7.0,
            "其他", 4.0
    );

    private FitnessCalorieCalculator() {
    }

    public static double estimate(String fitnessType, String intensity, int durationMinutes) {
        if (durationMinutes <= 0) {
            return 0;
        }
        double met = MET_BY_TYPE.getOrDefault(fitnessType, 4.0);
        double intensityFactor = switch (intensity == null ? "" : intensity) {
            case "低强度" -> 0.85;
            case "高强度" -> 1.15;
            default -> 1.0;
        };
        double calories = met * 3.5 * DEFAULT_WEIGHT_KG / 200.0
                * durationMinutes * intensityFactor;
        return Math.round(calories * 10.0) / 10.0;
    }
}
