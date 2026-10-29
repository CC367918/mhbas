package com.mhbas.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Goal {
    private Long id;
    private Long userId;
    private String goalType;
    private String goalName;
    private Double targetValue; // 目标值
    private String unit;        // 单位
    private String period;        // 时期
    private Boolean enabled;        // 是否启用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
