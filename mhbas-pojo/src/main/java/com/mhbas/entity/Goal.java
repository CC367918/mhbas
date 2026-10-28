package com.mhbas.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Goal {
    private Long id;
    private Long userId;
    private String goalType;
    private String goalName;
    private Double targetValue;
    private String unit;        // 单位
    private String period;        // 时期
    private Boolean enabled;        // 是否启用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
