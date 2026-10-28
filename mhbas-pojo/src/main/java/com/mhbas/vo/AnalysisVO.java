package com.mhbas.vo;

import java.util.List;

public final class AnalysisVO {
    private AnalysisVO() {
    }

    public record Overview(
            List<String> labels,
            List<Metric> metrics,
            List<ChartSeries> charts,
            List<Suggestion> suggestions
    ) {
    }

    public record Metric(
            String name,
            String unit,
            String recentAvgText,
            String baselineAvgText,
            String changeText,
            boolean trendUp,
            boolean trendDown,
            String level,
            String levelClass
    ) {
    }

    public record ChartSeries(
            String name,
            String unit,
            List<Integer> series,
            double recentAvg,
            double baselineAvg
    ) {
    }

    public record Suggestion(
            String name,
            String level,
            String levelClass,
            String severity,
            String advice
    ) {
    }
}
