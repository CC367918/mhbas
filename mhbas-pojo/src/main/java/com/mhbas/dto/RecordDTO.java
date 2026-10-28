package com.mhbas.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class RecordDTO {
    private RecordDTO() {
    }

    public record Step(
            @NotNull(message = "请选择日期")
            LocalDate recordDate,
            @NotNull(message = "请输入步数")
            @Min(value = 0, message = "步数不能小于 0")
            @Max(value = 100000, message = "单日步数不能超过 100000")
            Integer steps
    ) {
    }

    public record Fitness(
            @NotBlank(message = "请选择健身项目") String fitnessType,
            @NotNull(message = "请选择开始时间") LocalDateTime startTime,
            @NotNull(message = "请输入训练时长")
            @Min(value = 1, message = "训练时长至少 1 分钟")
            @Max(value = 1440, message = "训练时长不能超过 1440 分钟") Integer durationMinutes,
            @Size(max = 20, message = "强度参数过长") String intensity,
            @Size(max = 255, message = "备注不能超过 255 个字符") String note
    ) {
    }

    public record Sleep(
            @NotNull(message = "请选择日期") LocalDate recordDate,
            @NotNull(message = "请选择入睡时间") LocalTime sleepTime,
            @NotNull(message = "请选择起床时间") LocalTime wakeTime,
            @Size(max = 50, message = "主观状态不能超过 50 个字符") String subjectiveStatus
    ) {
    }

    public record Water(
            @NotNull(message = "请选择日期") LocalDate recordDate,
            @NotNull(message = "请选择饮水时间") LocalTime waterTime,
            @NotNull(message = "请输入饮水量")
            @Min(value = 1, message = "饮水量至少为 1ml")
            @Max(value = 5000, message = "单次饮水量不能超过 5000ml") Integer amountMl
    ) {
    }

    public record Meal(
            @NotNull(message = "请选择日期") LocalDate recordDate,
            @NotBlank(message = "请选择餐次") String mealType
    ) {
    }
}
