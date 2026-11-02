package com.mhbas.vo;

import java.util.List;

public final class RecordVO {
    private RecordVO() {
    }

    public record StepOverview(
            int todaySteps,
            int weekSteps,
            int averageSteps7,
            Integer goalDays7,  //达标天数；未设置目标时为 null
            double distanceKm,
            Integer goalSteps,
            Integer achievementRate,
            List<String> trendLabels,
            List<Integer> trendValues
    ) {
    }

    public record FitnessOverview(
            int weekCount,
            int weekMinutes,
            int weekCalories,
            List<String> trendLabels,
            List<Integer> trendValues,
            int maxTrend
    ) {
    }

    //睡眠界面信息
    public record SleepOverview(
            double averageHours,    //睡眠
            int fluctuationMinutes,  //波动分钟
            String regularity,       //规律性
            int recordCount,       //记录数
            List<String> trendLabels,
            List<Double> trendValues
    ) {
    }

    public record WaterOverview(
            int todayMl,
            int weekMl,
            Integer targetMl,
            Integer achievementRate,
            List<String> trendLabels,
            List<Integer> trendValues
    ) {
    }

    public record MealOverview(
            int todayCount,
            int weekCount,
            List<String> trendLabels,
            List<Integer> trendValues
    ) {
    }
}
