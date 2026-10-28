package com.mhbas.vo;

import com.mhbas.entity.FitnessRecord;
import com.mhbas.entity.MealRecord;
import com.mhbas.entity.SleepRecord;
import com.mhbas.entity.StepRecord;
import com.mhbas.entity.WaterRecord;

import java.util.List;

public final class RecordVO {
    private RecordVO() {
    }

    public record StepStats(
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

    public record StepOverview(List<StepRecord> records, StepStats stats) {
    }

    public record FitnessStats(
            int weekCount,
            int weekMinutes,
            int weekCalories,
            List<String> trendLabels,
            List<Integer> trendValues,
            int maxTrend
    ) {
    }

    public record FitnessOverview(List<FitnessRecord> records, FitnessStats stats) {
    }
    //睡眠界面信息
    public record SleepStats(
            double averageHours,    //睡眠
            int fluctuationMinutes,  //波动分钟
            String regularity,       //规律性
            int recordCount,       //记录数
            List<String> trendLabels,
            List<Double> trendValues
    ) {
    }

    public record SleepOverview(List<SleepRecord> records, SleepStats stats) {
    }

    public record WaterStats(
            int todayMl,
            int weekMl,
            Integer targetMl,
            Integer achievementRate,
            List<String> trendLabels,
            List<Integer> trendValues
    ) {
    }

    public record WaterOverview(List<WaterRecord> records, WaterStats stats) {
    }

    public record MealStats(
            int todayCount,
            int weekCount,
            List<String> trendLabels,
            List<Integer> trendValues
    ) {
    }

    public record MealOverview(List<MealRecord> records, MealStats stats) {
    }
}
