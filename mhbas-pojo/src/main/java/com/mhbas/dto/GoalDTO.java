package com.mhbas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class GoalDTO {
    private GoalDTO() {
    }

    public record Create(
            @NotBlank(message = "请选择目标类型") String goalType,
            @NotNull(message = "请输入目标值")
            @DecimalMin(value = "0.01", message = "目标值必须大于 0") Double targetValue
    ) {
    }
}
