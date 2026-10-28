package com.mhbas.vo;

public final class GoalVO {
    private GoalVO() {
    }

    public record TypeOption(String type, String name, String unit, String period, double suggestedTarget) {
    }

    public record Detail(
            Long id,
            String goalType,
            String goalName,
            Double targetValue,
            String unit,
            String period,
            Boolean enabled,
            double currentValue,
            int progress,
            boolean achieved
    ) {
    }
}
