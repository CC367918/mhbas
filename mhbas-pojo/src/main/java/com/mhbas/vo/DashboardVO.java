package com.mhbas.vo;

import java.util.List;

public final class DashboardVO {
    private DashboardVO() {
    }

    public record TodayStats(
            int sleepMinutes,
            int fitnessCount,
            int fitnessMinutes,
            int calories,
            int steps,
            Integer stepsGoal,
            int waterMl,
            int mealCount
    ) {
    }

    public record Trend7(
            List<String> labels,
            List<Integer> sleep,
            List<Integer> fitness,
            List<Integer> steps,
            List<Integer> water,
            List<Integer> meal
    ) {
    }

    public record WeeklySummary(
            int fitnessCount,
            int fitnessMinutes,
            double averageSleepHours,
            int steps,
            int waterMl
    ) {
    }

    public record Overview(
            TodayStats today,
            Trend7 trend,
            WeeklySummary weekly,
            List<GoalVO.Detail> goals,
            AnalysisVO.Overview analysis
    ) {
    }
}
